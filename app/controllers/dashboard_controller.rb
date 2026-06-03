class DashboardController < ApplicationController
  def index
    @range_days = (params[:days] || 30).to_i
    @since = Date.today - @range_days.days

    @sessions_by_date = Current.user
      .sleep_sessions
      .where(bucketed_date: @since..)
      .includes(:sleep_events)
      .order(bucketed_date: :asc)
      .group_by(&:bucketed_date)
  end
end
