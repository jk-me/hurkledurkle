# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `bin/rails
# db:schema:load`. When creating a new database, `bin/rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema[8.1].define(version: 2026_06_02_211002) do
  # These are extensions that must be enabled in order to support this database
  enable_extension "pg_catalog.plpgsql"

  create_table "sessions", force: :cascade do |t|
    t.datetime "created_at", null: false
    t.string "ip_address"
    t.datetime "updated_at", null: false
    t.string "user_agent"
    t.bigint "user_id", null: false
    t.index ["user_id"], name: "index_sessions_on_user_id"
  end

  create_table "sleep_events", force: :cascade do |t|
    t.datetime "created_at", null: false
    t.integer "event_type", null: false
    t.datetime "occurred_at", null: false
    t.bigint "sleep_session_id", null: false
    t.datetime "updated_at", null: false
    t.index ["sleep_session_id"], name: "index_sleep_events_on_sleep_session_id"
  end

  create_table "sleep_sessions", force: :cascade do |t|
    t.date "bucketed_date", null: false
    t.datetime "created_at", null: false
    t.boolean "is_nap", default: false, null: false
    t.datetime "rise_at"
    t.string "timezone", null: false
    t.datetime "updated_at", null: false
    t.bigint "user_id", null: false
    t.datetime "wind_down_at", null: false
    t.index ["user_id", "bucketed_date"], name: "index_sleep_sessions_on_user_id_and_bucketed_date"
    t.index ["user_id"], name: "index_sleep_sessions_on_user_id"
  end

  create_table "users", force: :cascade do |t|
    t.time "bedtime_reset_time"
    t.datetime "created_at", null: false
    t.string "email_address", null: false
    t.string "password_digest", null: false
    t.string "timezone"
    t.datetime "updated_at", null: false
    t.index ["email_address"], name: "index_users_on_email_address", unique: true
  end

  add_foreign_key "sessions", "users"
  add_foreign_key "sleep_events", "sleep_sessions"
  add_foreign_key "sleep_sessions", "users"
end
