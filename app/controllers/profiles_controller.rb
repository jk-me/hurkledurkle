class ProfilesController < ApplicationController
  def show
    redirect_to edit_profile_path
  end

  def edit
    @user = Current.user
  end

  def update
    @user = Current.user

    if @user.update(profile_params)
      redirect_to edit_profile_path, notice: "Profile updated."
    else
      render :edit, status: :unprocessable_entity
    end
  end

  private

  def profile_params
    params.require(:user).permit(:timezone, :bedtime_reset_time)
  end
end
