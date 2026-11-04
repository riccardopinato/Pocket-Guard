# CAP-0158 — Physical sensor QA and promotion checklist (Pockara)
Date: 2026-11-04
Scope: Pocket-Guard/Pockara Android; pure Kotlin EWMA/hysteresis live-sample shadow consumer.
Baseline source: Pocket-Guard main @ 1b10e39801190c3356cc5077973b2c6c96b9c8e0
Reference: App-Factory-Core @ ae76e4267b67fe0e913d650d8e89a010dd235a5c
Golden admission tracking: https://github.com/riccardopinato/App-Factory-Core/issues/68
Donor PR: https://github.com/riccardopinato/Pocket-Guard/pull/6
Build evidence: https://github.com/riccardopinato/Pocket-Guard/actions/runs/41819397260 (postmerge SUCCESS)

## Status and safety boundary

- Implemented/tested: scalar Kotlin EWMA/hysteresis parity with Core, and shadow consumption of real motion scores from SmartProtectionEngine.
- NOT implemented: using the shadow signal to arm, sound the alarm or change protective decisions. Existing SmartProtectionEngine confidence/transition logic is authoritative.
- No physical OEM tests recorded here. A CI build and emulator do not certify sensor reliability, foreground service behavior or battery.
- Do not silently enable shadow gating, increase alarm aggressiveness, or call the app "false-positive optimized" before collecting reproducible real-device evidence.
- On any observed false alarm, suspend promotion and preserve the reproduction conditions; never treat one passing session as a universal guarantee.
- Tests are opt-in on personal test devices. Do not use an audible alarm where it would be disruptive; coordinate owner/disarm and protect accounts.

## Required sample evidence per test run

Record a **redacted** test sheet (no personal content, location trace or raw user data):

- Test identifier; test date/time and timezone.
- Device manufacturer, model, Android version/build, OEM battery restrictions state, relevant sensor presence: proximity, accelerometer, light, gyroscope.
- APK package/versionCode/versionName, signed-artifact SHA-256, tested source commit SHA, build run URL; note sideload vs Play.
- Mode: POCKET / BAG; sensitivity LOW/NORMAL/HIGH; calibrated profile revision and sensor inventory (no personal calibration secrets).
- Starting app state, power-saving policy, foreground/background/locked state, test duration, action sequence, number of legitimate removals, number of expected/actual OWNER_CHECK and ALARM, false positives, misses, and unexpected service restart.
- Device power estimate: collect battery before/after and comparable idle control, sampling window, OS battery restrictions, thermal/battery saver; distinguish rough OS estimates from instrumented energy measurements.
- Outcome PASS / FAIL / BLOCKED / NOT VERIFIED, reproduction steps, issue URL, log/attachment checksum when permitted.

## Physical test matrix (every tested OEM)

| Case | Procedure | Expected evidence |
|---|---|---|
| P01 Idle dark pocket | Arm, leave device in stable pocket; vary normal posture | Protected state stable; no spurious owner check/alarm |
| P02 Pocket removal | Confirm storage, extract deliberately; repeat in both fast and slow movement | Expected extraction state transition and owner confirmation recorded |
| P03 Proximity-only | Uncover proximity without meaningful motion/light change | No unproven immediate extraction or alarm |
| P04 Light transient | Briefly expose light while device remains protected | No single-sample alarm; recovery observable |
| P05 Motion transient | Brief walking/jostle while device remains stored | No alarm from one transient; return-to-safe |
| P06 Bag mode | Repeat with BAG calibration, light absent/present and ordinary carrying | Correct degraded capability behavior; no claim all sensors available |
| P07 Aborted removal | Begin extracting, then return device to protected state before confirmation | Correct cancellation/reset and no late alarm |
| P08 Owner verification | Trigger owner check and verify with configured method | Correct disarm/re-arm; no stale EWMA state |
| P09 LOCK/BG lifecycle | Lock screen, foreground/background, restart service and app when permitted | No duplicated listeners, unexpected gaps or immediate alarm |
| P10 Missing sensors | Test a real device without optional sensors or simulate missing capability through existing diagnostics | Graceful degradation or clear block for required sensor |
| P11 Sensitivity ordering | Compare LOW/NORMAL/HIGH using comparable movements | Explainable detection tradeoff; no threshold inversion |
| P12 Calibration change | Recalibrate while safely disarmed; repeat P01/P02 | Reset shadow state, no stale profile behavior |
| P13 Signal validity | Observe service around sensor interruption, timestamp gaps, invalid sampling where testable | Finite math, fail-safe reset and no stale transition |
| P14 Sustained battery | Compare identical protection-on and control windows with same environmental conditions | Reported power delta, wake locks and process lifetime; no unsupported battery claim |
| P15 OEM restrictions | Repeat background tests with documented vendor battery restrictions | Reliability limitations made explicit to user |

## Suggested measurement protocol

1. Use at least two distinct Android manufacturers/OEM families; record each device separately. A model/Android version change is new evidence, not automatically inherited proof.
2. Freeze build identity (SHA and signed artifact checksum) before testing. Keep P01–P15 results tied to **exact bytes**, not merely versionName.
3. Capture a minimum of 10 deliberate removal trials and 20 benign movement/light trials per tested mode/device as a *sampling target*, not a statistically certified false-positive rate.
4. Include a stationary 30-minute protected session and a comparable 30-minute control window to observe unexpected wakeups and estimate energy usage; log environmental differences.
5. Verify process/service restart and no duplicate listeners. Re-arm, cancel and stop across multiple cycles.
6. If a trial fails, attach a redacted reproducible trace and file an issue rather than averaging away a safety-critical false positive.
7. Do not activate adaptive thresholds in protection logic until the tested results justify a new scoped PR, with regression tests and a new physical QA cycle for that exact artifact.

## Promotion decision template

- Scope demonstrated: **Kotlin scalar EWMA transition** / **real device sensor shadow** / **actual alarm integration** (select only those proven).
- OEM coverage: manufacturer + model + Android version + sensor capabilities.
- Success/fail summary: expected triggers / actual triggers / false positives / misses / unexpected restarts, separately by mode.
- Power summary: estimate method and limitations, comparative windows.
- Source and artifact identity: full SHA-256 and 40-char Git commit.
- Relevant unit/CI evidence links and physical trial IDs.
- Unverified/out-of-scope items: explicitly list.
- Decision: KEEP SHADOW / LIMITED CANDIDATE / REJECT / PROPOSE ACTIVATION PR.
- Golden Registry/Index status remains unchanged until a separately reviewed App-Factory-Core promotion PR passes its gates.

No tests in this document are claimed to have been physically executed.
