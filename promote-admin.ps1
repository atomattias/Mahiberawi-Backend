# Promote User to Super Admin Script
Write-Host "Promoting mattiasgebrie@gmail.com to Super Admin" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"

Write-Host "`nPromoting user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$email" -Method POST
    Write-Host "SUCCESS - User Promotion: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - User Promotion: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nPromotion attempt complete!" -ForegroundColor Green 