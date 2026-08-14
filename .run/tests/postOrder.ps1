# 1. Запрашиваем новый JWT токен в user-service через Gateway
$authBody = @{
    email    = "test@test.com"
    password = "123"
} | ConvertTo-Json -Compress

#$authResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
 #   -Method Post `
  #  -ContentType "application/json; charset=utf-8" `
   # -Body $authBody

#$token = $authResponse.token
$token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMDAiLCJyb2xlIjoiUk9MRV9VU0VSIiwiZXhwIjoxODAwMDAwMDAwfQ.3GbopCFRHi_AKXnimhy4rhC9y6-vgMyrz6fLVzG0zZY"

# 2. Формируем тело заказа
$orderBody = @{
    userId      = 1
    description = "Суши сет"
    price       = 1200.00
} | ConvertTo-Json -Compress

# 3. Отправляем запрос с переданным токеном
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/orders" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $orderBody