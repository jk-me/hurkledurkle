class SleepSession < ApplicationRecord
  belongs_to :user
  has_many :sleep_events, dependent: :destroy

  validates :wind_down_at, presence: true
  validates :timezone, presence: true
  validates :bucketed_date, presence: true
  validates :is_nap, inclusion: { in: [ true, false ] }

  before_validation :set_bucketed_date, on: :create

  def sleep_at
    sleep_events.sleep.order(:occurred_at).first&.occurred_at
  end

  def wake_at
    sleep_events.wake.order(:occurred_at).last&.occurred_at
  end

  def pre_sleep_minutes
    minutes_between(wind_down_at, sleep_at)
  end

  def rest_minutes
    minutes_between(sleep_at, wake_at)
  end

  def hurkle_durkle_minutes
    minutes_between(wake_at, rise_at)
  end

  def total_minutes
    minutes_between(wind_down_at, rise_at)
  end

  private

  def set_bucketed_date
    return unless wind_down_at && user

    reset_time = user.bedtime_reset_time || Time.zone.parse("12:00")
    local_wind_down = wind_down_at.in_time_zone(timezone)
    cutoff = local_wind_down.change(hour: reset_time.hour, min: reset_time.min, sec: 0)

    self.bucketed_date = local_wind_down < cutoff ? local_wind_down.to_date - 1 : local_wind_down.to_date
  end

  def minutes_between(start_time, end_time)
    return unless start_time && end_time

    ((end_time - start_time) / 60).round
  end
end
