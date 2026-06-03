class User < ApplicationRecord
  has_secure_password
  has_many :sessions, dependent: :destroy
  has_many :sleep_sessions, dependent: :destroy

  normalizes :email_address, with: ->(e) { e.strip.downcase }

  validates :timezone, inclusion: { in: ActiveSupport::TimeZone.all.map(&:name), allow_blank: true }
end
