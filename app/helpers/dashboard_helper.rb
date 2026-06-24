module DashboardHelper
  TIMING_SERIES = {
    wind_down: { label: "Wind-down", color: "#6366f1" },
    sleep: { label: "Sleep", color: "#2563eb" },
    wake: { label: "Wake", color: "#f59e0b" },
    rise: { label: "Rise", color: "#ef4444" }
  }.freeze

  CALCULATED_SERIES = {
    pre_sleep: { label: "Pre-sleep", color: "#8b5cf6" },
    rest: { label: "Rest", color: "#10b981" },
    hurkle_durkle: { label: "Hurkle-durkle", color: "#f97316" }
  }.freeze

  def display_local_time(time, time_zone_name)
    return "—" unless time

    time.in_time_zone(time_zone_name).strftime("%H:%M")
  end

  def display_minutes(minutes)
    return "—" unless minutes

    "#{minutes / 60}h #{minutes % 60}m"
  end

  def dashboard_sleep_time(session)
    session&.sleep_at
  end

  def dashboard_wake_time(session)
    session&.wake_at
  end

  def timing_graph_svg(chart_dates, main_sessions_by_date, time_zone_name)
    series_values = TIMING_SERIES.transform_keys(&:to_s).transform_values do |config|
      chart_dates.map do |date|
        session = main_sessions_by_date[date]
        time_for_series(session, config[:label].downcase.tr("-", "_").to_sym)
      end
    end

    build_time_chart_svg(chart_dates, series_values, TIMING_SERIES.transform_keys(&:to_s), time_zone_name)
  end

  def calculated_graph_svg(chart_dates, main_sessions_by_date, selected = :summary)
    series_set = if selected.to_sym == :summary
      CALCULATED_SERIES
    else
      CALCULATED_SERIES.slice(selected.to_sym)
    end

    series_values = series_set.transform_keys(&:to_s).transform_values do |config|
      chart_dates.map do |date|
        session = main_sessions_by_date[date]
        minutes_for_series(session, config[:label].downcase.tr("-", "_").to_sym)
      end
    end

    build_duration_chart_svg(chart_dates, series_values, series_set.transform_keys(&:to_s))
  end

  private

  def build_time_chart_svg(chart_dates, series_values, series_config, time_zone_name)
    width = 860
    height = 320
    left = 52
    top = 18
    bottom = 36
    right = 18
    plot_width = width - left - right
    plot_height = height - top - bottom
    step_x = chart_dates.size > 1 ? plot_width.to_f / (chart_dates.size - 1) : plot_width.to_f
    
    content_tag(:svg, viewBox: "0 0 #{width} #{height}", class: "chart-svg", role: "img", aria: { label: "Sleep timing chart" }) do
      safe_join([
        content_tag(:rect, nil, x: 0, y: 0, width: width, height: height, fill: "white"),
        time_axis_labels(left, top, plot_height),
        x_axis_labels(chart_dates, left, top, plot_height, step_x),
        series_lines(series_values, series_config, left, top, plot_height, step_x, :time, time_zone_name)
      ])
    end
  end

  def build_duration_chart_svg(chart_dates, series_values, series_config)
    width = 860
    height = 320
    left = 52
    top = 18
    bottom = 36
    right = 18
    plot_width = width - left - right
    plot_height = height - top - bottom
    step_x = chart_dates.size > 1 ? plot_width.to_f / (chart_dates.size - 1) : plot_width.to_f
    max_minutes = [ series_values.values.flatten.compact.max.to_i, 60 ].max

    content_tag(:svg, viewBox: "0 0 #{width} #{height}", class: "chart-svg", role: "img", aria: { label: "Calculated sleep values chart" }) do
      safe_join([
        content_tag(:rect, nil, x: 0, y: 0, width: width, height: height, fill: "white"),
        duration_axis_labels(left, top, plot_height, max_minutes),
        x_axis_labels(chart_dates, left, top, plot_height, step_x),
        series_lines(series_values, series_config, left, top, plot_height, step_x, :duration, max_minutes)
      ])
    end
  end

  def time_axis_labels(left, top, plot_height)
    safe_join([ 8, 10, 12, 2, 4, 6, 8, 10, 12, 14 ].map.with_index do |hour, idx|
      y = top + plot_height - (idx / 10.0 * plot_height)
      safe_join([
        content_tag(:line, nil, x1: left, y1: y, x2: 842, y2: y, stroke: "#e5e7eb", "stroke-width": 1),
        content_tag(:text, format("%02d:00", hour % 24), x: 8, y: y + 4, fill: "#6b7280", "font-size": 11)
      ])
    end)
  end

  def duration_axis_labels(left, top, plot_height, max_minutes)
    step = [ ((max_minutes / 4.0) / 30).ceil * 30, 30 ].max
    labels = (0..max_minutes).step(step).to_a

    safe_join(labels.map do |minutes|
      y = top + plot_height - (minutes.to_f / max_minutes * plot_height)
      safe_join([
        content_tag(:line, nil, x1: left, y1: y, x2: 842, y2: y, stroke: "#e5e7eb", "stroke-width": 1),
        content_tag(:text, display_minutes(minutes), x: 8, y: y + 4, fill: "#6b7280", "font-size": 11)
      ])
    end)
  end

  def x_axis_labels(chart_dates, left, top, plot_height, step_x)
    safe_join(chart_dates.each_with_index.map do |date, index|
      x = left + (index * step_x)
      content_tag(:text, date.strftime("%-m/%-d"), x: x, y: top + plot_height + 20, fill: "#6b7280", "font-size": 10, transform: "rotate(35 #{x} #{top + plot_height + 20})")
    end)
  end

  def series_lines(series_values, series_config, left, top, plot_height, step_x, mode, extra)
    safe_join(series_values.map do |name, values|
      points = values.each_with_index.filter_map do |value, index|
        next unless value

        x = left + (index * step_x)
        y = if mode == :time
          top + plot_height - (minutes_of_day(value, extra) / 1200.0 * plot_height) # 10 steps × 120 min = 1200 min scale
        else
          max_minutes = extra
          top + plot_height - (value.to_f / max_minutes * plot_height)
        end

        [ x.round(2), y.round(2) ]
      end

      config = series_config[name]
      safe_join([
        (content_tag(:polyline, nil, points: points.map { |x, y| "#{x},#{y}" }.join(" "), fill: "none", stroke: config[:color], "stroke-width": 3) if points.any?),
        safe_join(points.map do |x, y|
          content_tag(:circle, nil, cx: x, cy: y, r: 4, fill: config[:color])
        end)
      ].compact)
    end)
  end

  def minutes_of_day(time, time_zone_name)
    local_time = time.in_time_zone(time_zone_name)
    raw = (local_time.hour * 60) + local_time.min
    # Return minutes since 20:00 (8pm) to align with the axis window (20:00–14:00 next day).
    # Evening times (>=20:00) are offset from 8pm; morning/afternoon times (<20:00)
    # wrap around midnight by adding the remaining minutes in the day.
    raw >= 20 * 60 ? raw - (20 * 60) : raw + (24 * 60) - (20 * 60)
  end

  def time_for_series(session, series)
    return unless session

    case series
    when :wind_down then session.wind_down_at
    when :sleep then session.sleep_at
    when :wake then session.wake_at
    when :rise then session.rise_at
    end
  end

  def minutes_for_series(session, series)
    return unless session

    case series
    when :pre_sleep then session.pre_sleep_minutes
    when :rest then session.rest_minutes
    when :hurkle_durkle then session.hurkle_durkle_minutes
    end
  end
end
