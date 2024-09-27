package main

import (
    "encoding/csv"
    "fmt"
    "log"
    "os"
    "strconv"

    "github.com/go-echarts/go-echarts/v2/charts"
    "github.com/go-echarts/go-echarts/v2/opts"
    // "github.com/go-echarts/go-echarts/v2/types"
)

func main() {
    var file *os.File
    args := os.Args

    if len(args) < 1{
        fmt.Println("No path to csv passed")
        return
    } else {
        var err error
        file, err = os.Open(args[1])
        if err != nil {
            log.Fatal(err)
        }
    }
    defer file.Close()

    // Parse the CSV file
    reader := csv.NewReader(file)
    records, err := reader.ReadAll()
    if err != nil {
        log.Fatal(err)
    }

    // Prepare data for the plot
    var entryIDs []string
    var durations []opts.BarData

    // Skip the header row and loop through the data
    for i, record := range records {
        if i == 0 {
            // Skip the header row
            continue
        }

        // Parse entryId, jobStartTime, and jobEndTime
        entryID := record[0]
        jobStartTime, err := strconv.ParseInt(record[1], 10, 64)
        if err != nil {
            log.Fatal(err)
        }
        jobEndTime, err := strconv.ParseInt(record[2], 10, 64)
        if err != nil {
            log.Fatal(err)
        }

        // Calculate job duration in nanoseconds and convert to seconds

        // duration := float64(jobEndTime-jobStartTime) * math.Pow(10, -9)
        duration := float64(jobEndTime-jobStartTime)

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
            Type: "slider",
            XAxisIndex: []int{0},
            Start:      0,
            End:        100,
        }),
    )

    // Set the X-axis (entryId) and Y-axis (durations)
    bar.SetXAxis(entryIDs).
        AddSeries("Job Duration (s)", durations)


    // Render the chart to an HTML file
    fileName := "job_duration.html"
    f, err := os.Create(fileName)
    if err != nil {
        log.Fatal(err)
    }
    defer f.Close()

    // Save the chart to an HTML file
    bar.Render(f)
    fmt.Printf("Job duration chart has been generated to '%s'\n",fileName)
}

