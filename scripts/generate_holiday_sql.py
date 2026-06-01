import json
import glob
import os

BASE_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "holiday")
OUTPUT = os.path.join(os.path.dirname(__file__), "..", "sql", "when_rule_holidays_init.sql")


def main():
    rows = []
    pattern = os.path.join(BASE_DIR, "*", "*.json")
    for path in sorted(glob.glob(pattern)):
        country = os.path.basename(os.path.dirname(path)).upper()
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        for day in data.get("days", []):
            name = day["name"].replace("'", "''")
            date = day["date"]
            is_off = 1 if day.get("isOffDay") else 0
            rows.append(f"('{name}', '{country}', '{date}', {is_off})")

    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    with open(OUTPUT, "w", encoding="utf-8") as f:
        f.write("-- WhenRule 节假日数据初始化脚本\n")
        f.write("-- 来源: src/main/resources/holiday/\n")
        f.write(f"-- 共 {len(rows)} 条记录\n\n")
        f.write("INSERT INTO when_rule_holidays (name, country, `date`, is_off_day) VALUES\n")
        f.write(",\n".join(rows))
        f.write(";\n")

    print(f"Generated {len(rows)} rows -> {OUTPUT}")


if __name__ == "__main__":
    main()
