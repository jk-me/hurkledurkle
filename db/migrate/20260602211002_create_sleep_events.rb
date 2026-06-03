class CreateSleepEvents < ActiveRecord::Migration[8.1]
  def change
    create_table :sleep_events do |t|
      t.references :sleep_session, null: false, foreign_key: true
      t.integer :event_type, null: false
      t.datetime :occurred_at, null: false, precision: 6

      t.timestamps
    end
  end
end
