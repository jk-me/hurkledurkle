## Functional

- sleep tracker
- track 4 points manually, pure manual input only
- make a graph to view each month, or last x days

## Non-Functional / Stack

- Rails + Hotwire (Turbo + Stimulus) for web, with a mobile-responsive frontend
- Hotwire Native for mobile — the web views are reused inside native wrappers
- Project structure:
  - `/` — Rails app (web + Hotwire frontend, serves mobile web views too)
  - `/android` — Android app wrapper (Hotwire Native for Android)
- PostgreSQL database
- Email and password authentication

## Tracking Points (per sleep cycle)

- **Wind-down time** — when you started preparing for bed
- **Sleep time** — when you fell asleep
- **Wake time** — when you woke up
- **Rise time** — when you got out of bed

- Can add extra sleep/wake pairs for interrupted sleep within a cycle
- Can log multiple full cycles per day (e.g. naps)
- Each event is logged with a timestamp in a specific timezone
- User has a configurable default timezone on their account; used as default when logging events

## Data Viewing

- Calculate rest time for every day. Set a configurable **bedtime reset time** (per user) to bucket all sleep/naps into a given day
- Line graph: each of the four tracking points connected across days (x-axis = days, y-axis = time of day)
  - For interrupted sleep: connect the sleep time closest to wind-down, and the wake time closest to rise time, as the primary line
  - Additional interrupted sleep/wake pairs shown as individual points on that day's column
  - User can click a day on the graph to edit that day's entries
- Graph of calculated metrics: rest time, wind-down time (sleep − wind-down), hurkle-durkle time (rise − wake)
  - y-axis is total duration in hours and minutes (e.g. 7h 30m), x-axis is days

## Data Model

**Recommendation: session-based with child events**

```
sleep_sessions
  id            - uuid
  user_id       - uuid
  bucketed_date - date (ISO date only, no time or timezone)
  wind_down_at  - timestamptz (stored as UTC)
  rise_at       - timestamptz (stored as UTC)
  timezone      - string (IANA tz name, e.g. "America/Toronto") — records the user's local timezone at time of logging, used for display
  is_nap        - boolean, default false

sleep_events  (for interrupted sleep within a session)
  id            - uuid
  session_id    - uuid
  event_type    - enum (sleep | wake)
  occurred_at   - timestamptz (stored as UTC)
```

- All timestamp fields (`wind_down_at`, `rise_at`, `occurred_at`) are `timestamptz` — PostgreSQL stores UTC internally and converts on read
- `timezone` is kept as a separate column so times always display in the timezone they were logged in, regardless of the user's current timezone setting

- Each sleep cycle = one `sleep_session` row with `wind_down_at` and `rise_at` as the outer bounds
- Interrupted sleep/wake pairs are stored as child `sleep_events` rows on that session
- `is_nap`: set by the user when logging a session; defaults to `false` (unchecked)
- `date` is computed at write time from `wind_down_at` using the user's bedtime reset time + timezone setting, so daily queries are a simple `WHERE date = ?` — no inefficiency

**Why not flat events?**
A flat `sleep_events` table (one row per point type) would require reassembling sessions at query time and makes the "closest sleep to wind-down" graph logic harder. Session-based keeps that grouping explicit and queries fast.
