# CampusOverflow 冒烟测试脚本
# 使用方法：powershell -File scripts/smoke-test.ps1

$BASE = "http://localhost:8080"
$pass = 0; $fail = 0

function Check($name, $resp) {
    if ($resp.StatusCode -lt 400) {
        Write-Host "[PASS] $name" -ForegroundColor Green
        $script:pass++
    } else {
        Write-Host "[FAIL] $name" -ForegroundColor Red
        $script:fail++
    }
}

Write-Host "=== CampusOverflow 冒烟测试 ==="

# 1. 获取CSRF Token
try {
    $csrf = Invoke-WebRequest -Uri "$BASE/api/v1/auth/csrf" -SessionVariable s -UseBasicParsing
    Check "获取CSRF" $csrf
} catch { Check "获取CSRF" $_; exit }

# 2. 登录
try {
    $token = $s.Cookies["XSRF-TOKEN"].Value
    Write-Host "CSRF Token: $token"
    $login = Invoke-WebRequest -Uri "$BASE/swagger-ui.html" -UseBasicParsing
    Check "Swagger UI可访问" $login
} catch { Check "Swagger UI" $_ }

# 3. 健康检查
try {
    $health = Invoke-WebRequest -Uri "$BASE/actuator/health" -UseBasicParsing
    Check "健康检查" $health
} catch { Check "健康检查" $_ }

# 4. 问题列表
try {
    $list = Invoke-WebRequest -Uri "$BASE/api/v1/questions?page=1&size=5" -WebSession $s -UseBasicParsing
    Check "问题列表" $list
} catch { Check "问题列表" $_ }

Write-Host ""
Write-Host "=== 结果: $pass 通过, $fail 失败 ==="
