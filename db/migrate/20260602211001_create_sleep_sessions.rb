class CreateSleepSessions < ActiveRecord::Migration[8.1]
  def change
    create_table :sleep_sessions do |t|
      t.references :user, null: false, foreign_key: true
      t.date :bucketed_date, null: false
      t.datetime :wind_down_at, null: false, precision: 6
      t.datetime :rise_at, precision: 6
      t.string :timezone, null: false
      t.boolean :is_nap, null: false, default: false

      t.timestamps
    end

    add_index :sleep_sessions, [ :user_id, :bucketed_date ]
  end
end
