function New-RepoReleaseTag {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^\d+\.\d+\.\d+$')]
        [string]$Version,
        [switch]$Push,
        [switch]$DryRun
    )
    $scriptPath = Join-Path $PSScriptRoot 'release-tag.ps1'
    if (-not (Test-Path $scriptPath)) {
        throw "Could not find release-tag.ps1 at $scriptPath"
    }
    $arguments = @{
        Version = $Version
    }
    if ($Push) {
        $arguments.Push = $true
    }
    if ($DryRun) {
        $arguments.DryRun = $true
    }
    & $scriptPath @arguments
}

function New-RepoRelease {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^\d+\.\d+\.\d+$')]
        [string]$Version,
        [switch]$Push,
        [switch]$DryRun
    )

    $scriptPath = Join-Path $PSScriptRoot 'new-release.ps1'
    if (-not (Test-Path $scriptPath)) {
        throw "Could not find new-release.ps1 at $scriptPath"
    }

    $arguments = @{
        Version = $Version
    }
    if ($Push) {
        $arguments.Push = $true
    }
    if ($DryRun) {
        $arguments.DryRun = $true
    }

    & $scriptPath @arguments
}

Set-Alias -Name repo-release-tag -Value New-RepoReleaseTag -Scope Global
Set-Alias -Name rtag -Value New-RepoReleaseTag -Scope Global
Set-Alias -Name repo-release -Value New-RepoRelease -Scope Global
Set-Alias -Name nrelease -Value New-RepoRelease -Scope Global

Write-Host 'Loaded release tag commands:'
Write-Host ' - New-RepoReleaseTag -Version <x.y.z> [-Push] [-DryRun]'
Write-Host ' - repo-release-tag -Version <x.y.z> [-Push] [-DryRun]'
Write-Host ' - rtag -Version <x.y.z> [-Push] [-DryRun]'
Write-Host 'Loaded one-shot release commands:'
Write-Host ' - New-RepoRelease -Version <x.y.z> [-Push] [-DryRun]'
Write-Host ' - repo-release -Version <x.y.z> [-Push] [-DryRun]'
Write-Host ' - nrelease -Version <x.y.z> [-Push] [-DryRun]'
Write-Host ''
Write-Host 'This script only affects the current PowerShell session.'
Write-Host 'To make it permanent, add the following line to your PowerShell profile manually:'
Write-Host ('. "{0}"' -f (Join-Path $PSScriptRoot 'release-tag-alias.ps1'))
