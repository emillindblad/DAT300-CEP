package metrics

import (
	"fmt"
	"math"
	"sort"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

type TimeEntry struct {
	milliSecond int
	avgLatency  float64
	nrOfEvents  int
}

func PlotJobLatency(records [][]string) *charts.Line {
	fmt.Println("Creating latency plot")
	denominator := 1000000000 // ms=1000000, s =1000000000
	buckets := make(map[int]TimeEntry)

	for i, record := range records {
		// Skip the header row
		if i == 0 {
			continue
		}

		jobStartTime := ParseCsvStrToInt(record[1])
		jobEndTime := ParseCsvStrToInt(record[2])

		duration := float64(jobEndTime-jobStartTime) * math.Pow(10, -6)

		interval := int(jobEndTime) / denominator
		timeEntry := getOrCreate(buckets, interval)

		timeEntry.nrOfEvents++

		newAVG := (timeEntry.avgLatency*float64(timeEntry.nrOfEvents-1) + duration) / float64(timeEntry.nrOfEvents)
		timeEntry.milliSecond = interval
		timeEntry.avgLatency = newAVG

		buckets[interval] = timeEntry

	}

	var minMS, maxMS int
	for k := range buckets {
		if minMS == 0 || k < minMS {
			minMS = k
		}
		if k > maxMS {
			maxMS = k
		}
	}

	var keys []int
	for k := range buckets {
		keys = append(keys, k)
	}
	sort.Ints(keys)

	var xAxis []int
	var yAxis []opts.LineData

	// Extract values in sorted order
	for _, key := range keys {
		entry := buckets[key]
		// Normalize the milliSecond value
		normalizedMS := entry.milliSecond - minMS + 1 // Normalize to range 1 to x
		xAxis = append(xAxis, normalizedMS)
		yAxis = append(yAxis, opts.LineData{Value: entry.avgLatency})
	}

	averageLatency := calculateAverageLatency(buckets)
	line := charts.NewLine()

	// Set the chart title and axis labels
	line.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{
			Title:    "AVG Latency",
			Subtitle: fmt.Sprintf("Latency (ms) Avg %f ms\n", averageLatency),
		}),
		charts.WithXAxisOpts(opts.XAxis{
			Name: "Time (s)",
		}),
		charts.WithYAxisOpts(opts.YAxis{
			Name:      "",
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
		AddSeries("AVG latency (MS)", yAxis)

	fmt.Printf("Average Latency: %.2f ms\n", averageLatency)
	return line
}

func getOrCreate(buckets map[int]TimeEntry, key int) TimeEntry {
	if entry, exists := buckets[key]; exists {
		return entry
	}
	newEntry := TimeEntry{
		avgLatency: 0,
		nrOfEvents: 0,
	}
	buckets[key] = newEntry

	return newEntry
}

func calculateAverageLatency(buckets map[int]TimeEntry) float64 {
	if len(buckets) == 0 {
		return 0
	}

	totalLatency := 0.0
	count := 0
	for _, entry := range buckets {
		totalLatency += entry.avgLatency
		count++
	}
	return totalLatency / float64(count)
}
