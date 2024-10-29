package metrics

import (
	"fmt"
	"sort"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

type TimeEntryQ struct {
	second       int
	nrOfEvents   int
	avgQueueSize float64
}

func PlotJobQ(records [][]string) *charts.Line {
	fmt.Println("Creating queue plot")
	denominator := 1000000000 // ms=1000000, s =1000000000
	buckets := make(map[int]TimeEntryQ)

	for i, record := range records {
		// Skip the header row
		if i == 0 {
			continue
		}

		jobStartTime := ParseCsvStrToInt(record[1])
		queueSize := ParseCsvStrToInt(record[3])

		// Calculate job duration in nanoseconds and convert to milliseconds seconds
		// duration := float64(jobEndTime-jobStartTime)

		interval := int(jobStartTime) / denominator
		timeEntry := getOrCreateQ(buckets, interval)

		timeEntry.nrOfEvents++

		newAvgQueue := (float64(timeEntry.avgQueueSize)*float64(timeEntry.nrOfEvents-1) + float64(queueSize)) / float64(timeEntry.nrOfEvents)
		timeEntry.second = interval
		timeEntry.avgQueueSize = newAvgQueue

		buckets[interval] = timeEntry
	}

	var minS, maxS int
	for k := range buckets {
		if minS == 0 || k < minS {
			minS = k
		}
		if k > maxS {
			maxS = k
		}
	}

	var keys []int
	for k := range buckets {
		keys = append(keys, k)
	}
	sort.Ints(keys) // Sort the keys in ascending order

	var xAxis []int
	var yAxis []opts.LineData

	// Extract values in sorted order
	for _, key := range keys {
		entry := buckets[key]
		// Normalize the milliSecond value
		normalizedMS := entry.second - minS + 1 // Normalize to range 1 to x
		xAxis = append(xAxis, normalizedMS)
		yAxis = append(yAxis, opts.LineData{Value: entry.avgQueueSize})
	}

	line := charts.NewLine()

	// Set the chart title and axis labels
	line.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{
			Title:    "AVG Queue",
			Subtitle: "Queue (events)",
		}),
		charts.WithXAxisOpts(opts.XAxis{
			Name: "Time (s)",
		}),
		charts.WithYAxisOpts(opts.YAxis{
			Name:      "", // Primary Y-axis
			Position:  "left",
			AxisLine:  &opts.AxisLine{Show: opts.Bool(true)}, // Show axis line
			SplitLine: &opts.SplitLine{Show: opts.Bool(true)},
		}),
		charts.WithDataZoomOpts(opts.DataZoom{
			Type:       "slider",
			XAxisIndex: []int{0},
			Start:      0,
			End:        100,
		}),
	)

	// Set the X-axis (entryId) and Y-axis (durations)
	line.SetXAxis(xAxis).
		AddSeries("AVG Queue (events)", yAxis)
	return line
}

func getOrCreateQ(buckets map[int]TimeEntryQ, key int) TimeEntryQ {
	if entry, exists := buckets[key]; exists {
		return entry
	}

	newEntry := TimeEntryQ{
		nrOfEvents:   0,
		avgQueueSize: 0,
	}
	buckets[key] = newEntry

	return newEntry
}
