# Test script for event endpoints
$baseUrl = "http://localhost:8080/api"

Write-Host "Testing Event Endpoints..." -ForegroundColor Green

# Test 1: Get user's group events (was returning 500)
Write-Host "`n1. Testing GET /api/groups/user/events..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/groups/user/events" -Method GET -Headers @{
        "Authorization" = "Bearer YOUR_JWT_TOKEN_HERE"
        "Content-Type" = "application/json"
    }
    Write-Host "✅ SUCCESS: User group events endpoint working" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "❌ ERROR: User group events endpoint failed" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Message: $($_.Exception.Message)"
}

# Test 2: Get group-specific events (was returning 500)
Write-Host "`n2. Testing GET /api/groups/{groupId}/events..." -ForegroundColor Yellow
try {
    $groupId = "test-group-id" # Replace with actual group ID
    $response = Invoke-RestMethod -Uri "$baseUrl/groups/$groupId/events" -Method GET -Headers @{
        "Authorization" = "Bearer YOUR_JWT_TOKEN_HERE"
        "Content-Type" = "application/json"
    }
    Write-Host "✅ SUCCESS: Group events endpoint working" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "❌ ERROR: Group events endpoint failed" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Message: $($_.Exception.Message)"
}

# Test 3: Health check to verify server is running
Write-Host "`n3. Testing health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✅ SUCCESS: Server is running" -ForegroundColor Green
    Write-Host "Health: $($response | ConvertTo-Json)"
} catch {
    Write-Host "❌ ERROR: Server not responding" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)"
}

Write-Host "`nTest completed!" -ForegroundColor Green 