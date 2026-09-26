#!/usr/bin/env bash
# ==============================================================================
#  Ride Service - Automated API Verification Script (Bash cURL)
#  Target Host: http://localhost:9090
#  Microservice: Spring Boot 3.2.5 (Java 17, MongoDB)
# ==============================================================================

BASE_URL="http://localhost:9090/api/rides"
PASSED=0
FAILED=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

report_result() {
  local test_id="$1"
  local test_name="$2"
  local status="$3"
  local details="$4"

  if [ "$status" -eq 0 ]; then
    echo -e " [${GREEN}PASS${NC}] ${test_id} - ${test_name} | ${details}"
    PASSED=$((PASSED + 1))
  else
    echo -e " [${RED}FAIL${NC}] ${test_id} - ${test_name} | ${details}"
    FAILED=$((FAILED + 1))
  fi
}

echo -e "${CYAN}==================================================================${NC}"
echo -e "${CYAN} RIDE-SERVICE AUTOMATED CONTAINER API VERIFICATION SUITE (cURL)${NC}"
echo -e "${CYAN} Target Base URL: ${BASE_URL}${NC}"
echo -e "${CYAN}==================================================================${NC}\n"

# 1. Create Ride (Positive - 201)
echo -e "${YELLOW}[SCENARIO 1] POST /api/rides (Create Ride - Positive)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{"passengerId":"pass-101","pickupLocation":"123 Main St","dropoffLocation":"456 Market St"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 201 ]; then
  report_result "T1.1" "Status Code 201 Created" 0 "HTTP ${HTTP_CODE}"
else
  report_result "T1.1" "Status Code 201 Created" 1 "Expected 201, got ${HTTP_CODE}"
fi

RIDE_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
RIDE_STATUS=$(echo "$BODY" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

if [ "$RIDE_STATUS" == "REQUESTED" ]; then
  report_result "T1.2" "Initial Status REQUESTED" 0 "Status: ${RIDE_STATUS}"
else
  report_result "T1.2" "Initial Status REQUESTED" 1 "Got status: ${RIDE_STATUS}"
fi

if [ -n "$RIDE_ID" ]; then
  report_result "T1.3" "Ride ID Generated" 0 "Generated ID: ${RIDE_ID}"
else
  report_result "T1.3" "Ride ID Generated" 1 "No ID present in response"
fi

# 2. Update Status to ASSIGNED with driverId (Positive - 200)
echo -e "\n${YELLOW}[SCENARIO 2] PATCH /api/rides/${RIDE_ID}/status (Assign Driver - Positive)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/${RIDE_ID}/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"ASSIGNED","driverId":"drv-505"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
  report_result "T2.1" "Status Code 200 OK" 0 "HTTP ${HTTP_CODE}"
else
  report_result "T2.1" "Status Code 200 OK" 1 "Expected 200, got ${HTTP_CODE}"
fi

NEW_STATUS=$(echo "$BODY" | grep -o '"status":"[^"]*' | cut -d'"' -f4)
DRIVER_ID=$(echo "$BODY" | grep -o '"driverId":"[^"]*' | cut -d'"' -f4)

if [ "$NEW_STATUS" == "ASSIGNED" ]; then
  report_result "T2.2" "Status Updated to ASSIGNED" 0 "Status: ${NEW_STATUS}"
else
  report_result "T2.2" "Status Updated to ASSIGNED" 1 "Got status: ${NEW_STATUS}"
fi

if [ "$DRIVER_ID" == "drv-505" ]; then
  report_result "T2.3" "Driver ID Saved" 0 "Driver ID: ${DRIVER_ID}"
else
  report_result "T2.3" "Driver ID Saved" 1 "Got driverId: ${DRIVER_ID}"
fi

# 3. Create Ride Blank Fields (Negative - 400)
echo -e "\n${YELLOW}[SCENARIO 3] POST /api/rides (Blank Fields - Negative)${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{"passengerId":"","pickupLocation":"","dropoffLocation":""}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 400 ]; then
  report_result "T3.1" "Blank Fields Status 400 Bad Request" 0 "HTTP ${HTTP_CODE}"
else
  report_result "T3.1" "Blank Fields Status 400 Bad Request" 1 "Expected 400, got ${HTTP_CODE}"
fi

# 4. Invalid Status Transition REQUESTED -> COMPLETED (Negative - 400)
echo -e "\n${YELLOW}[SCENARIO 4] PATCH /api/rides/status (Invalid Transition - Negative)${NC}"
TEMP_RESP=$(curl -s -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{"passengerId":"pass-102","pickupLocation":"789 Broadway","dropoffLocation":"101 5th Ave"}')
TEMP_ID=$(echo "$TEMP_RESP" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/${TEMP_ID}/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 400 ]; then
  report_result "T4.1" "Invalid Transition Status 400 Bad Request" 0 "HTTP ${HTTP_CODE}"
else
  report_result "T4.1" "Invalid Transition Status 400 Bad Request" 1 "Expected 400, got ${HTTP_CODE}"
fi

if [[ "$BODY" == *"Invalid status transition"* ]]; then
  report_result "T4.2" "Error Message Verified" 0 "Error: ${BODY}"
else
  report_result "T4.2" "Error Message Verified" 1 "Missing error text in ${BODY}"
fi

# 5. Attempt to Cancel COMPLETED Ride (Negative - 400)
echo -e "\n${YELLOW}[SCENARIO 5] PATCH /api/rides/status (Cancel COMPLETED Ride - Negative)${NC}"
curl -s -X PATCH "${BASE_URL}/${TEMP_ID}/status" -H "Content-Type: application/json" -d '{"status":"ASSIGNED","driverId":"drv-505"}' > /dev/null
curl -s -X PATCH "${BASE_URL}/${TEMP_ID}/status" -H "Content-Type: application/json" -d '{"status":"ACCEPTED"}' > /dev/null
curl -s -X PATCH "${BASE_URL}/${TEMP_ID}/status" -H "Content-Type: application/json" -d '{"status":"IN_PROGRESS"}' > /dev/null
curl -s -X PATCH "${BASE_URL}/${TEMP_ID}/status" -H "Content-Type: application/json" -d '{"status":"COMPLETED"}' > /dev/null

RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/${TEMP_ID}/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"CANCELLED"}')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 400 ]; then
  report_result "T5.1" "Cancel COMPLETED Status 400 Bad Request" 0 "HTTP ${HTTP_CODE}"
else
  report_result "T5.1" "Cancel COMPLETED Status 400 Bad Request" 1 "Expected 400, got ${HTTP_CODE}"
fi

if [[ "$BODY" == *"Invalid status transition"* ]]; then
  report_result "T5.2" "Error Message Verified" 0 "Error: ${BODY}"
else
  report_result "T5.2" "Error Message Verified" 1 "Missing error text in ${BODY}"
fi

echo -e "\n${CYAN}==================================================================${NC}"
echo -e "${CYAN} AUTOMATED TEST VERIFICATION SUMMARY${NC}"
echo -e " ${GREEN}Total Passed Assertions: ${PASSED}${NC}"
echo -e " ${RED}Total Failed Assertions: ${FAILED}${NC}"
echo -e "${CYAN}==================================================================${NC}"

if [ "$FAILED" -ne 0 ]; then
  exit 1
else
  exit 0
fi
