# Deploy OpenOLAT to Tomcat
# Run this script to deploy and start OpenOLAT

Write-Host "Deploying OpenOLAT to Tomcat..." -ForegroundColor Green

$tomcatPath = "C:\apache-tomcat-10.1.33"
$warFile = "target\openolat-lms-20.2-SNAPSHOT.war"
$targetWar = "$tomcatPath\webapps\olat.war"

# Set up environment
Write-Host "`nSetting up environment..." -ForegroundColor Cyan
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.13+11"
$env:Path = "C:\Program Files\Java\jdk-17.0.13+11\bin;" + $env:Path

# Verify Java
Write-Host "Java version:"
java -version

# Stop Tomcat if running
Write-Host "`nStopping Tomcat (if running)..." -ForegroundColor Cyan
$tomcatProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {$_.Path -like "*tomcat*"}
if ($tomcatProcess) {
    Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

# Clean webapps
Write-Host "Cleaning previous deployment..." -ForegroundColor Cyan
if (Test-Path "$tomcatPath\webapps\olat") {
    Remove-Item "$tomcatPath\webapps\olat" -Recurse -Force
}
if (Test-Path $targetWar) {
    Remove-Item $targetWar -Force
}

# Copy WAR file
Write-Host "Copying WAR file..." -ForegroundColor Cyan
Copy-Item $warFile $targetWar -Force

# Set Tomcat memory options
$env:CATALINA_OPTS = "-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -Djava.awt.headless=true"

# Start Tomcat
Write-Host "`nStarting Tomcat..." -ForegroundColor Cyan
Set-Location $tomcatPath
& ".\bin\catalina.bat" run

Write-Host "`nTomcat started!" -ForegroundColor Green
Write-Host "OpenOLAT will be available at: http://localhost:8080/olat" -ForegroundColor Yellow
Write-Host "Login: administrator / openolat" -ForegroundColor Yellow
