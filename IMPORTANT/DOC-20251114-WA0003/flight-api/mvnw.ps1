$ErrorActionPreference = 'Stop'

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$wrapperDir = Join-Path $PSScriptRoot '.mvn/wrapper'
$jarPath = Join-Path $wrapperDir 'maven-wrapper.jar'
$wrapperUrl = 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.1/maven-wrapper-3.1.1.jar'

if (!(Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

if (!(Test-Path $jarPath)) {
    Write-Host 'Downloading Maven Wrapper JAR...'
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $jarPath
}

$dir = $PSScriptRoot

& java "-Dmaven.multiModuleProjectDirectory=$dir" -cp "$jarPath" org.apache.maven.wrapper.MavenWrapperMain @args