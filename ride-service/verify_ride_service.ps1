# ==============================================================================
#  Ride Service - Automated API Verification Script (PowerShell)
#  Target Host: http://localhost:9090
#  Microservice: Spring Boot 3.2.5 (Java 17, MongoDB)
# ==============================================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:9090/api/rides"
$passed = 0
$failed = 0

function Report-Result {
    param(
        [string]$TestId,
        [string]$TestName,
        [bool]$Passed,
        [string]$Details
    )
    if ($Passed) {
        Write-Host " [$TestId] PASS - $TestName | $Details" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host " [$TestId] FAIL - $TestName | $Details" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " RIDE-SERVICE AUTOMATED CONTAINER API VERIFICATION SUITE" -ForegroundColor Cyan
Write-Host " Target Base URL: $baseUrl" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# ------------------------------------------------------------------------------
# Test 1: Positive Test - Create Ride (Expect 201 Created)
# ------------------------------------------------------------------------------
Write-Host "`n[SCENARIO 1] POST /api/rides (Create Ride - Positive)" -ForegroundColor Yellow
$createPayload = @{
    passengerId = "pass-101"
    pickupLocation = "123 Main St"
    dropoffLocation = "456 Market St"
} | ConvertTo-Json

try {
    $res = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" -Body $createPayload -UseBasicParsing
    $ride = $res.Content | ConvertFrom-Json
    
    Report-Result "T1.1" "Status Code 201" ($res.StatusCode -eq 201) "HTTP Status: $($res.StatusCode)"
    Report-Result "T1.2" "Initial Status REQUESTED" ($ride.status -eq "REQUESTED") "Ride Status: $($ride.status)"
    Report-Result "T1.3" "Ride ID Generated" (![string]::IsNullOrEmpty($ride.id)) "Generated ID: $($ride.id)"
    $rideId = $ride.id
} catch {
    Report-Result "T1" "Create Ride Exception" $false "Failed: $_"
}

# ------------------------------------------------------------------------------
# Test 2: Positive Test - Update status to ASSIGNED with driverId (Expect 200 OK)
# ------------------------------------------------------------------------------
Write-Host "`n[SCENARIO 2] PATCH /api/rides/{id}/status (Assign Driver - Positive)" -ForegroundColor Yellow
$assignPayload = @{
    status = "ASSIGNED"
    driverId = "drv-505"
} | ConvertTo-Json

try {
    $res = Invoke-WebRequest -Uri "$baseUrl/$rideId/status" -Method PATCH -ContentType "application/json" -Body $assignPayload -UseBasicParsing
    $ride = $res.Content | ConvertFrom-Json
    
    Report-Result "T2.1" "Status Code 200" ($res.StatusCode -eq 200) "HTTP Status: $($res.StatusCode)"
    Report-Result "T2.2" "Status Updated to ASSIGNED" ($ride.status -eq "ASSIGNED") "Status: $($ride.status)"
    Report-Result "T2.3" "Driver ID Saved" ($ride.driverId -eq "drv-505") "Driver ID: $($ride.driverId)"
} catch {
    Report-Result "T2" "Update Status ASSIGNED Exception" $false "Failed: $_"
}

# ------------------------------------------------------------------------------
# Test 3: Negative Test - Create Ride with Blank Fields (Expect 400 Bad Request)
# ------------------------------------------------------------------------------
Write-Host "`n[SCENARIO 3] POST /api/rides (Blank Validation - Negative)" -ForegroundColor Yellow
$blankPayload = @{
    passengerId = ""
    pickupLocation = ""
    dropoffLocation = ""
} | ConvertTo-Json

try {
    $res = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" -Body $blankPayload -UseBasicParsing
    Report-Result "T3.1" "Expected 400 Bad Request" $false "Unexpectedly succeeded with HTTP $($res.StatusCode)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    Report-Result "T3.1" "Status Code 400" ($statusCode -eq 400) "Caught Expected HTTP Status 400 Bad Request"
}

# ------------------------------------------------------------------------------
# Test 4: Negative Test - Invalid Status Transition (REQUESTED directly to COMPLETED)
# ------------------------------------------------------------------------------
Write-Host "`n[SCENARIO 4] PATCH /api/rides/{id}/status (Invalid Transition - Negative)" -ForegroundColor Yellow
# Create temporary ride in REQUESTED state
$tempPayload = @{
    passengerId = "pass-102"
    pickupLocation = "789 Broadway"
    dropoffLocation = "101 5th Ave"
} | ConvertTo-Json
$tempRes = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" -Body $tempPayload -UseBasicParsing
$tempRide = $tempRes.Content | ConvertFrom-Json
$tempRideId = $tempRide.id

$invalidJumpPayload = @{ status = "COMPLETED" } | ConvertTo-Json

try {
    $res = Invoke-WebRequest -Uri "$baseUrl/$tempRideId/status" -Method PATCH -ContentType "application/json" -Body $invalidJumpPayload -UseBasicParsing
    Report-Result "T4.1" "Expected 400 Bad Request" $false "Unexpectedly succeeded with HTTP $($res.StatusCode)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    
    Report-Result "T4.1" "Status Code 400" ($statusCode -eq 400) "HTTP Status: $statusCode"
    Report-Result "T4.2" "Error Message Verified" ($body -like "*Invalid status transition*") "Response Body: $body"
}

# ------------------------------------------------------------------------------
# Test 5: Negative Test - Attempt to Cancel an Already COMPLETED Ride
# ------------------------------------------------------------------------------
Write-Host "`n[SCENARIO 5] PATCH /api/rides/{id}/status (Cancel COMPLETED Ride - Negative)" -ForegroundColor Yellow
# Advance $tempRide from ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
$validTransitions = @("ASSIGNED", "ACCEPTED", "IN_PROGRESS", "COMPLETED")
foreach ($st in $validTransitions) {
    $stPayload = @{ status = $st; driverId = "drv-505" } | ConvertTo-Json
    $null = Invoke-WebRequest -Uri "$baseUrl/$tempRideId/status" -Method PATCH -ContentType "application/json" -Body $stPayload -UseBasicParsing
}

$cancelPayload = @{ status = "CANCELLED" } | ConvertTo-Json
try {
    $res = Invoke-WebRequest -Uri "$baseUrl/$tempRideId/status" -Method PATCH -ContentType "application/json" -Body $cancelPayload -UseBasicParsing
    Report-Result "T5.1" "Expected 400 Bad Request" $false "Unexpectedly succeeded with HTTP $($res.StatusCode)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    
    Report-Result "T5.1" "Status Code 400" ($statusCode -eq 400) "HTTP Status: $statusCode"
    Report-Result "T5.2" "Error Message Verified" ($body -like "*Invalid status transition*") "Response Body: $body"
}

# ------------------------------------------------------------------------------
# Test Summary Report
# ------------------------------------------------------------------------------
Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " AUTOMATED TEST VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host " Total Passed Assertions: $passed" -ForegroundColor Green
Write-Host " Total Failed Assertions: $failed" -ForegroundColor $(If ($failed -gt 0) { "Red" } Else { "Green" })
Write-Host "==================================================================" -ForegroundColor Cyan

if ($failed -gt 0) { exit 1 } else { exit 0 }
