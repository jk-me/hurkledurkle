class User < ApplicationRecord
  has_secure_password
  has_many :sessions, dependent: :destroy
  has_many :sleep_sessions, dependent: :destroy

  normalizes :email_address, with: ->(e) { e.strip.downcase }

  before_validation :set_defaults, on: :create

  validates :timezone, inclusion: { in: ActiveSupport::TimeZone.all.map(&:name), allow_blank: true }

  private

  def set_defaults
    self.timezone = timezone.presence || "UTC"
    self.bedtime_reset_time ||= Time.zone.parse("12:00")
  end
end
