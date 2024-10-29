package main

import (
	"encoding/csv"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"

	//"math/rand"
	"dat300/metrics"

	"github.com/go-echarts/go-echarts/v2/components"
)

func loadCsvFromDir(path string) [][]string {
	var records [][]string

	files, err := os.ReadDir(path)
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
	// sorting by starting timestamp in order to be able to fix the non-nique id to a new unique id
	sort.Slice(records, func(i, j int) bool {
		num1, err1 := strconv.Atoi(records[i][1])
		num2, err2 := strconv.Atoi(records[j][1])
		if err1 != nil || err2 != nil {
			log.Fatal("Failed to convert to integer:", err1, err2)
		}
		return num1 < num2
	})
	updateIds(records)
	return records
}

func printFirstAndLast10(records [][]string) {
	length := len(records)

	fmt.Println("First 10 records:")
	if length <= 10 {
		// If the slice has 10 or fewer elements, print the entire slice
		for _, record := range records {
			fmt.Println(record)
		}
	} else {
		// Print the first 10 elements
		for _, record := range records[:10] {
			fmt.Println(record)
		}
	}

	// Print the last 10 elements
	fmt.Println("\nLast 10 records:")
	if length > 10 {
		// If the slice has more than 10 elements, print the last 10
		for _, record := range records[length-10:] {
			fmt.Println(record)
		}
	}
}

func updateIds(records [][]string) {
	lastId := 0
	lastNewId := 0
	for _, job := range records {
		currentId := int(metrics.ParseCsvStrToInt(job[0]))
		newId := calcNewId(currentId, lastId, lastNewId)
		lastId = currentId
		lastNewId = newId
		job[0] = strconv.Itoa(newId)
	}
}

func calcNewId(currentId int, lastId int, lastNewId int) int {
	result := currentId - lastId // Calculate the difference

	if result < 0 {
		fmt.Println("The id is negative:", result)

		return lastNewId + currentId // the number of event since IDs were reset
	}

	return result + lastNewId // Return the calculated value
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
		// metrics.PlotJobDuration(records),
		metrics.PlotJobLatency(records),
		metrics.PlotThroughPut(records),
		metrics.PlotJobQ(records),
	)

	fileName := fmt.Sprintf("%s.html", getFilename(dirPath))

	f, err := os.Create(fileName)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	page.Render(f)
	fmt.Printf("Charts has been generated to '%s'\n", fileName)
}

func getFilename(path string) string {
	lastIndex := strings.LastIndexAny(path, "/\\")

	if lastIndex == -1 {
		return path
	}
	return path[lastIndex+1:]
}
