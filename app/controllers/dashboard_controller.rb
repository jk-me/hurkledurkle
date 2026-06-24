class DashboardController < ApplicationController
  def index
    @range_days = (params[:days] || 30).to_i
    @time_zone_name = Current.user.timezone.presence || "UTC"

    Time.use_zone(@time_zone_name) do
      @today = Time.zone.today
      @since = @today - @range_days.days
    end

    @sessions_by_date = Current.user
      .sleep_sessions
      .where(bucketed_date: @since..)
      .includes(:sleep_events)
      .order(bucketed_date: :desc)
      .group_by(&:bucketed_date)

    @chart_dates = (@since..@today).to_a
    @main_sessions_by_date = @sessions_by_date.transform_values do |sessions|
      sessions.find { |session| !session.is_nap } || sessions.first
    end
  end
end
