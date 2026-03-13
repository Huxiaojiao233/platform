#!/usr/bin/env python3
"""
地震数据模拟处理算法
"""
import json
import os
import time
import sys
from datetime import datetime

def process_earthquake_data(input_file, output_dir, threshold=0.5):
    """
    模拟地震数据处理

    Args:
        input_file: 输入数据文件路径
        output_dir: 输出目录路径
        threshold: 检测阈值
    """
    print(f"[{datetime.now()}] 开始处理地震数据...")
    print(f"输入文件: {input_file}")
    print(f"输出目录: {output_dir}")
    print(f"检测阈值: {threshold}")

    # 检查输入文件是否存在
    if not os.path.exists(input_file):
        print(f"错误: 输入文件 {input_file} 不存在!")
        return False

    # 读取输入数据
    try:
        with open(input_file, 'r') as f:
            if input_file.endswith('.json'):
                data = json.load(f)
            else:
                data = f.read()
        print(f"成功读取数据文件")
    except Exception as e:
        print(f"读取数据文件失败: {e}")
        return False

    # 模拟数据处理
    print("正在进行数据分析...")
    time.sleep(3)  # 模拟计算过程

    print("检测地震事件...")
    time.sleep(2)  # 模拟检测过程

    # 生成模拟结果
    result = {
        "processing_time": datetime.now().isoformat(),
        "input_file": input_file,
        "threshold": threshold,
        "detected_events": 42,  # 模拟检测到的地震事件数
        "magnitudes": [3.2, 4.1, 2.8, 3.5],  # 模拟震级
        "status": "success",
        "message": "数据处理完成"
    }

    # 生成详细报告
    report = f"""
地震数据处理报告
================
处理时间: {result['processing_time']}
输入数据: {os.path.basename(input_file)}
检测阈值: {threshold}

检测结果:
- 检测到地震事件: {result['detected_events']} 个
- 最大震级: {max(result['magnitudes']):.1f}
- 最小震级: {min(result['magnitudes']):.1f}

事件详情:
{chr(10).join([f'  - 震级: {m:.1f}' for m in result['magnitudes']])}
    """

    # 保存结果文件
    output_file = os.path.join(output_dir, 'result.json')
    report_file = os.path.join(output_dir, 'report.txt')

    with open(output_file, 'w') as f:
        json.dump(result, f, indent=2)

    with open(report_file, 'w') as f:
        f.write(report)

    print(f"\n结果已保存:")
    print(f"  - JSON结果: {output_file}")
    print(f"  - 文本报告: {report_file}")

    # 输出统计信息到控制台（会被Docker日志捕获）
    print(f"\n处理统计:")
    print(f"  - 检测事件数: {result['detected_events']}")
    print(f"  - 最大震级: {max(result['magnitudes']):.1f}")

    return True

def main():
    """主函数"""
    print("=" * 50)
    print("地震数据处理算法 v1.0")
    print("=" * 50)

    # 从环境变量获取参数
    threshold = float(os.getenv('DETECT_THRESHOLD', '0.5'))
    input_file = os.getenv('INPUT_FILE', '/data/input/seismic_data.json')
    output_dir = os.getenv('OUTPUT_DIR', '/data/output')

    # 执行处理
    success = process_earthquake_data(input_file, output_dir, threshold)

    if success:
        print("\n✅ 处理成功完成!")
        sys.exit(0)
    else:
        print("\n❌ 处理失败!")
        sys.exit(1)

if __name__ == "__main__":
    main()