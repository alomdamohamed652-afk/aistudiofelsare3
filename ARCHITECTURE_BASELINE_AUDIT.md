# Repository Baseline Audit

## Baseline
- Base commit: 769cf2f (main)
- Audit branch: fix/repository-clean-baseline

## Confirmed repair
- The tracked `gradle/wrapper/gradle-wrapper.jar` on main was an empty blob (SHA e69de29...), so `gradlew.bat` could not load `org.gradle.wrapper.GradleWrapperMain`.
- This branch restores a non-empty Gradle Wrapper JAR compatible with the project's Gradle 9.3.1 wrapper configuration.

## Current architecture notes
- `FalsareeApp.kt` now passes the required Auth and CustomerProfile contracts.
- OTP is intentionally only a temporary UI/testing flow and is not connected to a real backend API.
- Authentication/session ownership is implemented locally through Room and `LocalAuthRepository`.
- Do not merge overlapping historical fix branches blindly. Main must be treated as the single baseline, and every integration must be validated by a clean build.

## Required validation
Run:
```
./gradlew clean assembleDebug
```
on a clean checkout of this branch before merging to main.
