# Check User Role Script
Write-Host "Checking role for mattiasgebrie@gmail.com" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"

Write-Host "`nChecking user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/check-role/$email" -Method GET
    Write-Host "SUCCESS - Role Check: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Role Check: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nRole check complete!" -ForegroundColor Green 