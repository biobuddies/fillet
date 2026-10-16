# fillet

Android form filler: an [autofill service](https://developer.android.com/identity/autofill/autofill-services)
offering hard-coded profiles, so far one, The American Church in Paris.

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

Then open Fillet, which asks Android to make it the autofill service. Chrome also needs Settings,
Autofill services, Autofill using another service.
