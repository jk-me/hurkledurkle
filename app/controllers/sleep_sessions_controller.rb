class SleepSessionsController < ApplicationController
  before_action :set_sleep_session, only: %i[edit update destroy]

  def index
    @sleep_sessions = Current.user.sleep_sessions.order(bucketed_date: :desc)
  end

  def new
    @sleep_session = Current.user.sleep_sessions.build(timezone: Current.user.timezone)
  end

  def create
    @sleep_session = Current.user.sleep_sessions.build(sleep_session_params)
    @sleep_session.timezone ||= Current.user.timezone.presence || "UTC"

    if @sleep_session.save
      respond_to do |format|
        format.html { redirect_to dashboard_path, notice: "Sleep session logged." }
        format.turbo_stream
      end
    else
      render :new, status: :unprocessable_entity
    end
  end

  def edit
  end

  def update
    if @sleep_session.update(sleep_session_params)
      respond_to do |format|
        format.html { redirect_to dashboard_path, notice: "Session updated." }
        format.turbo_stream
      end
    else
      render :edit, status: :unprocessable_entity
    end
  end

  def destroy
    @sleep_session.destroy
    respond_to do |format|
      format.html { redirect_to dashboard_path, notice: "Session deleted." }
      format.turbo_stream
    end
  end

  private

  def set_sleep_session
    @sleep_session = Current.user.sleep_sessions.find(params[:id])
  end

  def sleep_session_params
    params.require(:sleep_session).permit(
      :wind_down_at, :rise_at, :timezone, :is_nap
    )
  end
end
