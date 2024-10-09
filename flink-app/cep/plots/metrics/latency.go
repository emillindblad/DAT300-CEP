package metrics

import (
	"fmt"
	"math"
	"sort"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

// Define a struct with three int fields
type TimeEntry struct {
    milliSecond int
    avgLatency float64
    nrOfEvents int
}

func PlotJobLatency(records [][]string) *charts.Line {
	fmt.Println("Creating latency plot")
	//var entryIDs []int
	//var durations []opts.BarData
	denominator := 1000000000 //ms=1000000, s =1000000000
	buckets := make(map[int]TimeEntry)

	for i, record := range records {
		// Skip the header row
		if i == 0 {
			continue
		}

		//entryID := i

		jobStartTime := ParseCsvStrToInt(record[1])
		jobEndTime := ParseCsvStrToInt(record[2])

		// Calculate job duration in nanoseconds and convert to milliseconds seconds
		duration := float64(jobEndTime-jobStartTime) * math.Pow(10, -6)
		// duration := float64(jobEndTime-jobStartTime)

		interval := int(jobEndTime) / denominator
		timeEntry := getOrCreate(buckets, interval)

		// Increment events count first
		timeEntry.nrOfEvents++

		// Calculate new average after incrementing
		newAVG := (timeEntry.avgLatency*float64(timeEntry.nrOfEvents-1) + duration) / float64(timeEntry.nrOfEvents)
		timeEntry.milliSecond = interval
		timeEntry.avgLatency = newAVG

		// Update the bucket with the new TimeEntry
		buckets[interval] = timeEntry

	}

	// Normalization Step
    var minMS, maxMS int
    for k := range buckets {
        if minMS == 0 || k < minMS {
            minMS = k
        }
        if k > maxMS {
            maxMS = k
        }
    }

	// Normalization Step

    // Set x to the largest value found minus the starting value
    //x := maxMS - minMS
	var keys []int
	for k := range buckets {
		keys = append(keys, k)
	}
	sort.Ints(keys) // Sort the keys in ascending order

	var xAxis []int
	var yAxis []opts.LineData
	fmt.Println("latency data")
	fmt.Println("startTime", keys[0])
	fmt.Println("endTime", keys[len(keys)-1])
	fmt.Println("total ms", (keys[len(keys)-1]- keys[0]))

	// Extract values in sorted order
	for _, key := range keys {
		entry := buckets[key]
		// Normalize the milliSecond value
		normalizedMS := entry.milliSecond - minMS + 1 // Normalize to range 1 to x
		xAxis = append(xAxis, normalizedMS)
		yAxis = append(yAxis, opts.LineData{Value: entry.avgLatency})
	}

	line := charts.NewLine()

	// Set the chart title and axis labels
	line.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{
			Title:    "AVG Latency",
			Subtitle: "Latency (ms)",
		}),
		charts.WithXAxisOpts(opts.XAxis{
			Name: "Time (s)",
		}),
		charts.WithYAxisOpts(opts.YAxis{
			Name: "Latency (ms)", // Primary Y-axis
			Position: "left",
			AxisLine: &opts.AxisLine{Show: opts.Bool(true)}, // Show axis line
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
		

	return line
}

func getOrCreate(buckets map[int]TimeEntry, key int) TimeEntry {
    // Check if the key exists in the map
    if entry, exists := buckets[key]; exists {
        return entry // Return the existing entry
    }

    // If the key does not exist, create a new TimeEntry
    newEntry := TimeEntry{
        // Initialize fields as necessary
		avgLatency: 0,
		nrOfEvents: 0,
    }
    buckets[key] = newEntry // Add the new entry to the map

    return newEntry
}
