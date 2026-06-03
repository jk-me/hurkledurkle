class SleepEvent < ApplicationRecord
  belongs_to :sleep_session

  enum :event_type, { sleep: 0, wake: 1 }

  validates :event_type, presence: true
  validates :occurred_at, presence: true
end
