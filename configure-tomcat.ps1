# Configure Tomcat for OpenOLAT in VS Code
# This script creates the proper server.xml configuration

$tomcatPath = "C:\apache-tomcat-10.1.33"
$serverXml = "$tomcatPath\conf\server.xml"

Write-Host "Configuring Tomcat server.xml for OpenOLAT..." -ForegroundColor Green

# Backup original server.xml
if (-not (Test-Path "$serverXml.backup")) {
    Copy-Item $serverXml "$serverXml.backup"
    Write-Host "Backup created: server.xml.backup" -ForegroundColor Cyan
}

# Read server.xml
$xml = [xml](Get-Content $serverXml)

# Find the Host element
$hostElement = $xml.Server.Service.Engine.Host

# Check if Context already exists
$existingContext = $hostElement.Context | Where-Object { $_.path -eq "/olat" }

if ($existingContext) {
    Write-Host "Context for /olat already exists, updating..." -ForegroundColor Yellow
    $hostElement.RemoveChild($existingContext)
}

# Create new Context element
$context = $xml.CreateElement("Context")
$context.SetAttribute("docBase", "olat")
$context.SetAttribute("path", "/olat")
$context.SetAttribute("reloadable", "false")

# Create Resources element
$resources = $xml.CreateElement("Resources")
$resources.SetAttribute("allowLinking", "true")
$resources.SetAttribute("cacheMaxSize", "100000")
$resources.SetAttribute("cachingAllowed", "true")

# Add Resources to Context
$context.AppendChild($resources) | Out-Null

# Add Context to Host
$hostElement.AppendChild($context) | Out-Null

# Find and update Connector timeout (increase from default 20s to 180s)
$connector = $xml.Server.Service.Connector | Where-Object { $_.port -eq "8080" }
if ($connector) {
    $connector.SetAttribute("connectionTimeout", "180000")
    Write-Host "Connector timeout set to 180 seconds" -ForegroundColor Cyan
}

# Save the modified XML
$xml.Save($serverXml)

Write-Host "`nTomcat configuration complete!" -ForegroundColor Green
Write-Host "Configuration applied:" -ForegroundColor Cyan
Write-Host "  - Context path: /olat" -ForegroundColor White
Write-Host "  - Reloadable: false" -ForegroundColor White
Write-Host "  - Resources cache: 100MB" -ForegroundColor White
Write-Host "  - Connection timeout: 180s" -ForegroundColor White
