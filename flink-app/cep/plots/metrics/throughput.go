package metrics

import (
	"fmt"
	"sort"

	"github.com/go-echarts/go-echarts/v2/charts"
	"github.com/go-echarts/go-echarts/v2/opts"
)

func PlotThroughPut(records [][]string) *charts.Line {
	lastId := 0
	fmt.Println("Creating throughput plot")

	denominator := 1000000000 // ms=1000000, s =1000000000
	buckets := make(map[int]int)
	for _, job := range records {
		currentId := int(ParseCsvStrToInt(job[0]))
		processedEvents := calcProcessedObjects(currentId, lastId)
		lastId = currentId
		interval := int(ParseCsvStrToInt(job[2])) / denominator
		buckets[interval] += processedEvents
	}

	keys := make([]int, 0, len(buckets))
	for k := range buckets {
		keys = append(keys, k)
	}
	sort.Ints(keys)

	averageThroughput := calculateAverage(buckets)

	line := charts.NewLine()
	line.SetGlobalOptions(
		charts.WithTitleOpts(opts.Title{
			Title:    "System Throughput Over Time",
			Subtitle: fmt.Sprintf("Throughput (Events) Avg: %f events", averageThroughput),
		}),
		charts.WithXAxisOpts(opts.XAxis{
			Name: "Time (s)",
		}),
		charts.WithYAxisOpts(opts.YAxis{
			Name: "",
		}),
		charts.WithDataZoomOpts(opts.DataZoom{
			Type:       "slider",
			XAxisIndex: []int{0},
			Start:      0,
			End:        100,
		}),
	)

	var xAxis []int
	for i := range keys {
		xAxis = append(xAxis, i)
	}

	line.SetXAxis(xAxis).
		AddSeries("Throughput", generateLineItems(keys, buckets)).
		SetSeriesOptions(
			charts.WithLineChartOpts(opts.LineChart{}),
		)

	fmt.Printf("Average Throughput: %.2f events\n", averageThroughput)
	return line
}

func generateLineItems(keys []int, buckets map[int]int) []opts.LineData {
	items := make([]opts.LineData, 0)
	for _, key := range keys {
		items = append(items, opts.LineData{Value: buckets[key]})
	}
	return items
}

func calcProcessedObjects(x, y int) int {
	result := x - y

	if result < 0 {
		return x
	}

	return result
}

func calculateAverage(buckets map[int]int) float64 {
	total := 0
	count := 0

	for _, value := range buckets {
		total += value
		count++
	}

	if count == 0 {
		return 0
	}
	return float64(total) / float64(count)
}
