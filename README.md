# fillet

Android car stereo billboard: type album, artist, and track (37 characters each, a 2011 Honda
Odyssey row) and tap Set to show them over Bluetooth AVRCP. A nonempty Device renames the phone's
Bluetooth adapter; cars may show the new name only after reconnecting. Fillet loops silent audio so
Android routes its media session, not another player's, to the car.

Fillet also holds a dormant form filler: an
[autofill service](https://developer.android.com/identity/autofill/autofill-services) offering
hard-coded profiles, so far one, The American Church in Paris.

## Associations

Each profile lists association globs, matched against `https://example.org` for web pages
(scheme and domain; Android reports no path) or `android-app://org.example` for apps:

| Scope    | Glob                                                       |
| -------- | ---------------------------------------------------------- |
| All      | `*`                                                        |
| Multiple | `{https://example.org,android-app://org.example}`          |
| Multiple | `https://*.example.org`, `http{,s}://example.[co][ro][gm]` |
| One      | `android-app://org.example`                                |

`*` matches any run, `/` included; `?` one character; `[a-z]` and `[!a-z]` sets; `\` escapes.

## Install

```bash
mise build
adb install build/fillet.apk
```

For autofill, enable Fillet in Settings, Passwords & accounts. Chrome also needs Settings,
Autofill services, Autofill using another service.
