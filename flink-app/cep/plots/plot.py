import pandas as pd
import matplotlib.pyplot as plt

# Read the CSV file
csv_file = "../outSink/2024-09-26--18/out.csv"  # Path to your CSV file
data = pd.read_csv(csv_file)

# Calculate job duration in nanoseconds
data["jobDurationNs"] = data["endTime"] - data["startTime"]

# Convert job duration to seconds (or other time units if preferred)
# 1 nanosecond = 1e-9 seconds
data["jobDurationSec"] = data["jobDurationNs"] * 1e-9

# Plot the data
plt.figure(figsize=(10, 6))
plt.bar(data["id"], data["jobDurationNs"], color="skyblue")

# Add labels and title
plt.xlabel("Entry ID")
plt.ylabel("Job Duration (seconds)")
plt.title("Job Duration for Each Entry")

# Show the plot
plt.tight_layout()
plt.show()
