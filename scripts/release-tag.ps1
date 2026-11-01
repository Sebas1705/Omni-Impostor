param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version,

    [switch]$Push,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Exec-Git {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Args
    )

    $output = & git @Args 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Args -join ' ') failed: $output"
    }
    return $output
}

$repoRoot = (Exec-Git -Args @("rev-parse", "--show-toplevel")).Trim()
$currentBranch = (Exec-Git -Args @("rev-parse", "--abbrev-ref", "HEAD")).Trim()

if ($currentBranch -ne "main") {
    throw "Release tags must be created from 'main'. Current branch: $currentBranch"
}

$status = (& git status --porcelain)
if ($LASTEXITCODE -ne 0) {
    throw "Could not read git status."
}
if (-not $DryRun -and -not [string]::IsNullOrWhiteSpace(($status -join "").Trim())) {
    throw "Working tree is not clean. Commit or stash changes before tagging."
}

Exec-Git -Args @("fetch", "origin", "main", "--tags", "--quiet") | Out-Null

$tag = "v$Version"

if ((& git rev-parse -q --verify "refs/tags/$tag") 2>$null) {
    throw "Local tag already exists: $tag"
}

$remoteTag = & git ls-remote --tags origin "refs/tags/$tag"
if ($LASTEXITCODE -ne 0) {
    throw "Could not check remote tags."
}
if (-not [string]::IsNullOrWhiteSpace($remoteTag)) {
    throw "Remote tag already exists: $tag"
}

if ($DryRun) {
    Write-Host "[DRY-RUN] Repository: $repoRoot"
    Write-Host "[DRY-RUN] Tag to create: $tag"
    if ($Push) {
        Write-Host "[DRY-RUN] Command: git tag $tag; git push origin $tag"
    } else {
        Write-Host "[DRY-RUN] Command: git tag $tag"
        Write-Host "[DRY-RUN] Optional push: git push origin $tag"
    }
    exit 0
}

Exec-Git -Args @("tag", $tag) | Out-Null
Write-Host "Created local tag: $tag"

if ($Push) {
    Exec-Git -Args @("push", "origin", $tag) | Out-Null
    Write-Host "Pushed tag to origin: $tag"
} else {
    Write-Host "Tag not pushed. Run: git push origin $tag"
}
