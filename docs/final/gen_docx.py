from docx import Document
from docx.shared import Pt
from docx.oxml.ns import qn
from lxml import etree

def set_run_font(run, font_name='宋体', size=12):
    run.font.size = Pt(size)
    rPr = run._element.get_or_add_rPr()
    rFonts = rPr.find(qn('w:rFonts'))
    if rFonts is None:
        rFonts = etree.SubElement(rPr, qn('w:rFonts'))
    rFonts.set(qn('w:eastAsia'), font_name)
    rFonts.set(qn('w:ascii'), font_name)
    rFonts.set(qn('w:hAnsi'), font_name)

def add_heading(doc, text, level=1):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = True
    if level == 1:
        set_run_font(run, '黑体', 16)
    elif level == 2:
        set_run_font(run, '黑体', 14)
    else:
        set_run_font(run, '黑体', 12)
    return p

def add_text(doc, text, bold=False, size=12):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = bold
    set_run_font(run, '宋体', size)
    return p

doc = Document()

# 标题
add_heading(doc, '个人总结', level=1)
doc.add_paragraph()

# 基本信息
add_heading(doc, '基本信息', level=2)
add_text(doc, '姓名：石洹毓')
add_text(doc, '学号：2024117161')
add_text(doc, '班级：计算机科学与技术')
add_text(doc, '小组角色：Developer（开发 + 测试）')
doc.add_paragraph()

# 我做了什么
add_heading(doc, '我做了什么', level=2)

add_heading(doc, '1. 负责的模块', level=3)
add_text(doc, '我主要负责：')
add_text(doc, '• Reputation 声誉与激励模块（账本、徽章、悬赏、排行榜）')
add_text(doc, '• Discovery 内容发现模块（搜索、通知）')
add_text(doc, '• 前端页面开发（课程页、个人资料页、管理页、悬赏对话框、Markdown预览）')
doc.add_paragraph()

add_heading(doc, '2. 具体完成的工作', level=3)
works = [
    '1. 补写了 Reputation 应用层的幂等测试（ReputationEventHandlersTest，5个测试），覆盖开户、采纳、投票、徽章、通知的事件重放场景',
    '2. 补写了 Discovery 应用层的幂等测试（NotificationProjectorTest，2个测试），验证通知不会重复发送',
    '3. 修复了标签计数 bug：编辑问题时旧标签没有减1、新标签没有加1，导致标签数量统计不对',
    '4. 修复了回答删除时 answerCount 没有减1的 bug，同时补了审计记录',
    '5. 修复了投票时错误码不对的问题（QA-2101 改成 QA-1014）',
    '6. 前端新增了课程页面（我的课程、加入课程、全部课程）',
    '7. 前端补全了个人资料页（可以编辑学院和班级）',
    '8. 前端补全了管理后台（认证学生列表、创建教工账号）',
    '9. 前端把原来的 prompt() 弹窗改成了内联的悬赏对话框',
    '10. 前端加了 Markdown 实时预览面板',
    '11. 写了 DDD 设计文档（上下文映射、统一语言词汇表、事件目录）',
    '12. 写了 5 个 Sprint 的 Scrum 记录',
    '13. 打了 v1.0.0 版本标签，写了 Release Notes',
    '14. 走了完整的 PR 流程演示（feature 分支 → PR → 合并 → 清理分支）',
]
for w in works:
    add_text(doc, w)
doc.add_paragraph()

# 我学到了什么
add_heading(doc, '我学到了什么', level=2)

add_heading(doc, '技术收获', level=3)
techs = [
    '1. 学会了 DDD 领域驱动设计的基本思路，知道了什么是聚合根、值对象、领域事件、限界上下文',
    '2. 理解了事件最终一致性的概念，知道了为什么要用事务性发件箱模式',
    '3. 学会了写幂等测试，知道了分布式系统里重复消费是个大问题',
    '4. 熟悉了 Spring Boot 项目的分层结构（api / domain / application / infrastructure / interfaces）',
    '5. 学会了用 React + TypeScript 写前端页面，知道了路由、组件、状态管理是怎么回事',
    '6. 了解了 CI/CD 流水线是怎么跑的，知道了每次 push 都会自动跑测试',
]
for t in techs:
    add_text(doc, t)
doc.add_paragraph()

add_heading(doc, '过程收获', level=3)
process = [
    '1. 学会了怎么和组员分工协作，两个人怎么配合着做完一个项目',
    '2. 理解了 Scrum 敏捷开发的迭代思想，知道了每个 Sprint 做一块东西',
    '3. 知道了 Git 版本管理的重要性，学会了用分支和 PR 来协作',
    '4. 学会了写技术文档（ADR、Scrum记录、DDD文档），知道了代码写完还要写文档',
]
for p in process:
    add_text(doc, p)
doc.add_paragraph()

# 遇到的困难和解决方法
add_heading(doc, '遇到的困难和解决方法', level=2)
issues = [
    '1. 问题：标签计数不对，编辑问题后标签数量没更新\n   解决：检查 QuestionCommandService 的 edit 方法，发现是旧标签没有 -1、新标签没有 +1，修复后就对了',
    '2. 问题：Docker Desktop 一直启动不了，卡 Starting 状态，集成测试跑不了\n   解决：先装了 WSL2，但还是不行，最后决定先不折腾 Docker，把单元测试跑通就行，集成测试后面再说',
    '3. 问题：一开始不知道 PR 流程怎么走，直接在 main 上改代码\n   解决：后面专门走了一遍完整的 PR 流程演示（创建 feature 分支 → 推送 → 网页上创建 PR → 合并 → 清理分支），学会了标准流程',
    '4. 问题：应用层之前没有测试，不知道怎么写\n   解决：参考了领域层测试的写法，自己写了 Reputation 和 Discovery 的应用层幂等测试，验证事件重放不会重复加分',
]
for i in issues:
    add_text(doc, i)
doc.add_paragraph()

# 自我评价
add_heading(doc, '自我评价', level=2)

add_heading(doc, '做得好的地方', level=3)
goods = [
    '1. 代码写完后测试都跑过了，61个测试全绿，没有留明显的 bug',
    '2. 文档写得比较完整，DDD文档、Scrum记录、ADR都有',
    '3. 遇到问题能自己查资料解决，比如标签计数 bug 是自己定位到的',
]
for g in goods:
    add_text(doc, g)
doc.add_paragraph()

add_heading(doc, '需要改进的地方', level=3)
bads = [
    '1. 对 DDD 的理解还不够深入，很多概念是边做边学的，没有完全吃透',
    '2. Git 操作还不太熟练，有时候会搞混分支和远程的关系',
    '3. 前端的代码写得比较粗糙，没有做太多优化，比如移动端适配、性能优化这些都没做',
    '4. Docker 环境一直没搞通，集成测试没能跑起来，这是个遗憾',
]
for b in bads:
    add_text(doc, b)
doc.add_paragraph()

# 对课程的建议
add_heading(doc, '对课程的建议', level=2)
add_text(doc, '希望老师能多讲一点 DDD 的实际案例，感觉理论听着有点抽象，跟着做的时候才慢慢理解。另外 Docker 环境配置对新手来说有点复杂，希望能有更详细的指导。')
doc.add_paragraph()

# 总结
add_heading(doc, '总结', level=2)
add_text(doc, '通过这门课的贯穿项目，我第一次完整地经历了一个软件项目从需求到设计到开发到测试的全过程。虽然一开始很多东西都不懂，比如 DDD 是什么、事件为什么要发件箱、幂等测试怎么写，但跟着做下来慢慢就理解了。这个项目让我真正体会到了软件工程是怎么一回事，也学会了怎么用团队协作的方式做一个完整的系统。')

doc.save(r'C:\Users\lenovo\Desktop\campus\CampusOverflow\docs\final\个人总结-石洹毓.docx')
print('done')
