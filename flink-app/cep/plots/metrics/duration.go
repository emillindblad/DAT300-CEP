package metrics

import (
	"fmt"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

func PlotJobDuration(records [][]string) *charts.Bar {
	fmt.Println("Creating jobduration plot")
	var entryIDs []int
	var durations []opts.BarData

	for i, record := range records {
		// Skip the header row
		if i == 0 {
			continue
		}

		entryID := i

		jobStartTime := ParseCsvStrToInt(record[1])
		jobEndTime := ParseCsvStrToInt(record[2])

		// Calculate job duration in nanoseconds and convert to seconds
		duration := float64(jobEndTime-jobStartTime)
		// duration := float64(jobEndTime-jobStartTime)

		// Append data
		entryIDs = append(entryIDs, entryID)
		durations = append(durations, opts.BarData{Value: duration})
	}

	// Create a new bar chart
	bar := charts.NewBar()

	// Set the chart title and axis labels
	bar.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{
			Title:    "Job Duration for Each Entry",
			Subtitle: "Job durations in seconds",
		}),
		charts.WithXAxisOpts(opts.XAxis{
			Name: "Entry ID",
		}),
		charts.WithYAxisOpts(opts.YAxis{
			Name: "Job Duration (nano-seconds)",
		}),
		charts.WithDataZoomOpts(opts.DataZoom{
			Type:       "slider",
			XAxisIndex: []int{0},
			Start:      0,
			End:        100,
		}),
	)

	// Set the X-axis (entryId) and Y-axis (durations)
	bar.SetXAxis(entryIDs).
		AddSeries("Job Duration (s)", durations)

	return bar
}
