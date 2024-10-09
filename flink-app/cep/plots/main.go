package main

import (
	"encoding/csv"
	"fmt"
	"log"
	//"math/rand"
	"os"
	"path/filepath"
    "time"

	"dat300/metrics"

	"github.com/go-echarts/go-echarts/v2/components"
)

func loadCsvFromDir(path string) [][]string {
	var records [][]string

	files, err := os.ReadDir(path)
	fmt.Println(files)
	if err != nil {
		log.Fatal("Failed to read dir")
	}

	for _, file := range files {
		csvFilePath := filepath.Join(path, file.Name())

		file, err := os.Open(csvFilePath)
		if err != nil {
			log.Fatal(err)
		}
		defer file.Close()

		reader := csv.NewReader(file)
		record, err := reader.ReadAll()
		if err != nil {
			log.Fatal(err)
		}

		for _, data := range record {
			records = append(records, data)
		}

	}
	// TODO: Investigate sorting of files
	// sort.Slice(records, func(i, j int) bool {
	// 	return records[i][1] < records[j][1]
	// })
	return records
}

func main() {
	dirPath := os.Args[1]

	var records [][]string

	if len(dirPath) < 1 {
		fmt.Println("No path to csv passed")
		return
	} else {
		records = loadCsvFromDir(dirPath)
	}

	page := components.NewPage()
	page.AddCharts(
		 //metrics.PlotJobDuration(records),
		metrics.PlotJobLatency(records),
		metrics.PlotThroughPut(records),
	)

	// Render the charts to an HTML file
	// suffix := base64.StdEncoding.EncodeToString([]byte(randNum))
    t := time.Now()
	fileName := fmt.Sprintf("flink-charts-%s.html", t.Format("20060102150405"))

	f, err := os.Create(fileName)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	page.Render(f)
	fmt.Printf("Charts has been generated to '%s'\n", fileName)
}
