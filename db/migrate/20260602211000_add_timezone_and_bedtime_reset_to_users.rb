class AddTimezoneAndBedtimeResetToUsers < ActiveRecord::Migration[8.1]
  def change
    add_column :users, :timezone, :string
    add_column :users, :bedtime_reset_time, :time
  end
end
