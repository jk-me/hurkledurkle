class SleepEventsController < ApplicationController
  before_action :set_sleep_session

  def create
    @sleep_event = @sleep_session.sleep_events.build(sleep_event_params)
    if @sleep_event.save
      respond_to do |format|
        format.html { redirect_to edit_sleep_session_path(@sleep_session) }
        format.turbo_stream
      end
    else
      render turbo_stream: turbo_stream.replace(
        "sleep_event_form_#{@sleep_session.id}",
        partial: "sleep_events/form",
        locals: { sleep_session: @sleep_session, sleep_event: @sleep_event }
      ), status: :unprocessable_entity
    end
  end

  def destroy
    @sleep_event = @sleep_session.sleep_events.find(params[:id])
    @sleep_event.destroy
    respond_to do |format|
      format.html { redirect_to edit_sleep_session_path(@sleep_session) }
      format.turbo_stream { render turbo_stream: turbo_stream.remove("sleep_event_#{@sleep_event.id}") }
    end
  end

  private

  def set_sleep_session
    @sleep_session = Current.user.sleep_sessions.find(params[:sleep_session_id])
  end

  def sleep_event_params
    params.require(:sleep_event).permit(:event_type, :occurred_at)
  end
end
