class SleepSession < ApplicationRecord
  belongs_to :user
  has_many :sleep_events, dependent: :destroy

  validates :wind_down_at, presence: true
  validates :timezone, presence: true
  validates :bucketed_date, presence: true
  validates :is_nap, inclusion: { in: [ true, false ] }

  before_validation :set_bucketed_date, on: :create

  private

  def set_bucketed_date
    return unless wind_down_at && user

    reset_time = user.bedtime_reset_time || Time.parse("12:00")
    local_wind_down = wind_down_at.in_time_zone(timezone)
    cutoff = local_wind_down.change(hour: reset_time.hour, min: reset_time.min, sec: 0)

    self.bucketed_date = local_wind_down < cutoff ? local_wind_down.to_date - 1 : local_wind_down.to_date
  end
end
