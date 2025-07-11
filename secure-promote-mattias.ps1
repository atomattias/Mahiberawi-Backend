# Secure Super Admin Promotion Script for mattiasgebrie@gmail.com
Write-Host "Secure Promotion of mattiasgebrie@gmail.com to Super Admin" -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"

Write-Host "`nTarget user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

# Step 1: Get promotion key from user
Write-Host "`n1. Enter the promotion key (set in SUPER_ADMIN_PROMOTION_KEY environment variable):" -ForegroundColor Yellow
$promotionKey = Read-Host "Promotion Key" -AsSecureString
$promotionKeyPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($promotionKey))

# Step 2: Attempt secure promotion to Super Admin
Write-Host "`n2. Attempting secure promotion to Super Admin..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/secure-promote-super-admin/$email?promotionKey=$promotionKeyPlain" -Method POST
    Write-Host "SUCCESS - Secure Super Admin Promotion: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Secure Super Admin Promotion: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nSecure Super Admin promotion process complete!" -ForegroundColor Green 