Rails.application.routes.draw do
  resource :session
  resources :passwords, param: :token
  resource :registration, only: %i[new create]

  resources :sleep_sessions do
    resources :sleep_events, only: %i[create destroy]
  end

  get "dashboard" => "dashboard#index", as: :dashboard
  root "dashboard#index"

  get "up" => "rails/health#show", as: :rails_health_check
end
