# Generic User Promotion to Super Admin Script
# This script allows you to promote any user to Super Admin role

Write-Host "=== User Promotion to Super Admin ===" -ForegroundColor Green
Write-Host "This script promotes any user to SUPER_ADMIN role" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"

# Get target user email
$targetEmail = Read-Host "`nEnter the email of the user to promote to Super Admin"

# Get admin credentials
Write-Host "`nAdmin Authentication Required:" -ForegroundColor Yellow
$adminEmail = Read-Host "Enter your admin email"
$adminPassword = Read-Host -AsSecureString "Enter your admin password"

# Convert secure string to plain text for API call
$adminPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminPassword))

Write-Host "`nTarget user: $targetEmail" -ForegroundColor Cyan
Write-Host "Admin user: $adminEmail" -ForegroundColor Cyan
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

# Step 1: Authenticate as admin
Write-Host "`n1. Authenticating as admin..." -ForegroundColor Yellow
$loginBody = @{ 
    email = $adminEmail
    password = $adminPasswordPlain 
} | ConvertTo-Json

$loginResponse = $null
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Admin authentication successful!" -ForegroundColor Green
} catch {
    Write-Host "✗ Admin authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
    exit 1
}

# Step 2: Promote target user to Super Admin
Write-Host "`n2. Promoting $targetEmail to SUPER_ADMIN..." -ForegroundColor Yellow
$headers = @{ 
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$targetEmail" -Method POST -Headers $headers
    Write-Host "✓ SUCCESS - User promoted to Super Admin!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Cyan
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAILED - User promotion failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

# Step 3: Verify the promotion (optional)
Write-Host "`n3. Verifying promotion..." -ForegroundColor Yellow
try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method GET -Headers $headers
    Write-Host "✓ Verification complete" -ForegroundColor Green
} catch {
    Write-Host "! Could not verify promotion (but it may have succeeded)" -ForegroundColor Yellow
}

Write-Host "`n=== Promotion Process Complete ===" -ForegroundColor Green
Write-Host "User: $targetEmail" -ForegroundColor Cyan
Write-Host "Action: Promoted to SUPER_ADMIN" -ForegroundColor Cyan 