# Test script for specific group events endpoint
$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$groupId = "a1d93dff-e8c9-42ac-ab85-35a1a7ad57a9"

Write-Host "Testing Group Events Endpoint..." -ForegroundColor Green
Write-Host "Group ID: $groupId" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl" -ForegroundColor Cyan

# Test 1: Health check
Write-Host "`n1. Testing health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✅ SUCCESS: Server is running" -ForegroundColor Green
    Write-Host "Health: $($response | ConvertTo-Json)"
} catch {
    Write-Host "❌ ERROR: Health check failed" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Message: $($_.Exception.Message)"
}

# Test 2: Check if group exists
Write-Host "`n2. Testing group details endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/groups/$groupId" -Method GET -Headers @{
        "Authorization" = "Bearer YOUR_JWT_TOKEN_HERE"
        "Content-Type" = "application/json"
    }
    Write-Host "✅ SUCCESS: Group exists" -ForegroundColor Green
    Write-Host "Group: $($response | ConvertTo-Json -Depth 2)"
} catch {
    Write-Host "❌ ERROR: Group not found or access denied" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Message: $($_.Exception.Message)"
}

# Test 3: Test events endpoint
Write-Host "`n3. Testing group events endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/groups/$groupId/events" -Method GET -Headers @{
        "Authorization" = "Bearer YOUR_JWT_TOKEN_HERE"
        "Content-Type" = "application/json"
    }
    Write-Host "✅ SUCCESS: Group events endpoint working" -ForegroundColor Green
    Write-Host "Events: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "❌ ERROR: Group events endpoint failed" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)"
    Write-Host "Message: $($_.Exception.Message)"
    
    # Get error details
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nTest completed!" -ForegroundColor Green 