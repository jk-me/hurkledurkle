class SleepSessionsController < ApplicationController
  before_action :set_sleep_session, only: %i[edit update destroy]

  def index
    @sleep_sessions = Current.user.sleep_sessions.order(bucketed_date: :desc)
  end

  def new
    @sleep_session = Current.user.sleep_sessions.build(timezone: Current.user.timezone)
  end

  def create
    @sleep_session = Current.user.sleep_sessions.build(
      wind_down_at: sleep_session_params[:wind_down_at].in_time_zone(sleep_session_params[:timezone]),
      rise_at: sleep_session_params[:rise_at].in_time_zone(sleep_session_params[:timezone]),
      timezone: sleep_session_params[:timezone],
      is_nap: sleep_session_params[:is_nap]
    )
    @sleep_session.timezone ||= Current.user.timezone.presence || "UTC"

    if sleep_session_params[:sleep_at].present?
      @sleep_session.sleep_events.build(
        event_type: :sleep,
        occurred_at: sleep_session_params[:sleep_at].in_time_zone(@sleep_session.timezone)
      )
    end

    if sleep_session_params[:wake_at].present?
      @sleep_session.sleep_events.build(
        event_type: :wake,
        occurred_at: sleep_session_params[:wake_at].in_time_zone(@sleep_session.timezone)
      )
    end

    if @sleep_session.save && @sleep_session.sleep_events.all?(&:persisted?)
      redirect_to dashboard_path, notice: "Sleep session logged."
    else
      render :new, status: :unprocessable_entity
    end
  end

  def edit
  end

  def update
    if @sleep_session.update(
      wind_down_at: sleep_session_params[:wind_down_at].in_time_zone(sleep_session_params[:timezone]),
      rise_at: sleep_session_params[:rise_at].in_time_zone(sleep_session_params[:timezone]),
      timezone: sleep_session_params[:timezone],
      is_nap: sleep_session_params[:is_nap]
    )
      if sleep_session_params[:sleep_at].present?
        sleep_event = @sleep_session.sleep_events.find_or_initialize_by(event_type: :sleep)
        sleep_event.occurred_at = sleep_session_params[:sleep_at].in_time_zone(@sleep_session.timezone)
        sleep_event.save
      end

      if sleep_session_params[:wake_at].present?
        sleep_event = @sleep_session.sleep_events.find_or_initialize_by(event_type: :wake)
        sleep_event.occurred_at = sleep_session_params[:wake_at].in_time_zone(@sleep_session.timezone)
        sleep_event.save
      end

      redirect_to dashboard_path, notice: "Sleep session for #{@sleep_session.bucketed_date} updated."
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

  def build_fields(params)
    {
      wind_down_at: params[:wind_down_at].in_time_zone(params[:timezone]),
      rise_at: params[:rise_at].in_time_zone(params[:timezone]),
      timezone: params[:timezone],
      is_nap: params[:is_nap]
    }
  end

  def set_sleep_session
    @sleep_session = Current.user.sleep_sessions.find(params[:id])
  end

  def sleep_session_params
    params.require(:sleep_session).permit(
      :wind_down_at, :sleep_at,
      :wake_at, :rise_at, :timezone, :is_nap
    )
  end
end
