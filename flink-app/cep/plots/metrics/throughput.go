package metrics

import (
	"fmt"
	"sort"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

func PlotThroughPut(records [][]string) *charts.Line {
	fmt.Println("Creating throughput plot")

	denominator := 1000000000 //ms=1000000, s =1000000000
	startTime := ParseCsvStrToInt(records[1][1])
	endTime := ParseCsvStrToInt(records[len(records)-1][2])
	fmt.Println("startTime", startTime)
	fmt.Println("endTime", endTime)
	fmt.Println("total", (endTime-startTime)/int64(denominator))

	buckets := make(map[int]int)
	for _, job := range records {
		interval := int(ParseCsvStrToInt(job[2])) / denominator
		buckets[interval] += 1
	}

	/* for t := startTime; t <= endTime; t += interval {
		activeJobs := 0
		for _, job := range records {
			jobStart := parseCsvStrToInt(job[1])
			jobEnd := parseCsvStrToInt(job[2])
			if jobStart <= t && jobEnd > t {
				activeJobs++
			}
		}
		timePoints = append(timePoints, t)
		throughPut = append(throughPut, int(activeJobs))
	} */

	keys := make([]int, 0, len(buckets))
	for k := range buckets {
		keys = append(keys, k)
	}
	sort.Ints(keys)
	// fmt.Println(keys)

	line := charts.NewLine()
	line.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{Title: "System Throughput Over Time"}),
		charts.WithXAxisOpts(opts.XAxis{Name: "Time (s)"}),
		charts.WithYAxisOpts(opts.YAxis{Name: "Throughput (Events)"}),
		charts.WithDataZoomOpts(opts.DataZoom{
			Type:       "slider",
			XAxisIndex: []int{0},
			Start:      0,
			End:        100,
		}),
		// charts.WithTooltipOpts(opts.Tooltip{Show: true}),
	)

	var xAxis []int
	for i := range keys {
		// for i := 0; i <= len(buckets); i++ {
		xAxis = append(xAxis, i)
	}

	line.SetXAxis(xAxis).
		AddSeries("Throughput", generateLineItems(keys, buckets)).
		SetSeriesOptions(
			charts.WithLineChartOpts(opts.LineChart{}),
			// charts.WithLabelOpts(),
		)

	return line
}

func generateLineItems(keys []int, buckets map[int]int) []opts.LineData {
	items := make([]opts.LineData, 0)
	for _, key := range keys {
		items = append(items, opts.LineData{Value: buckets[key]})
	}
	return items
}