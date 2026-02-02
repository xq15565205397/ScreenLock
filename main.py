from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.clock import Clock
import time
import subprocess
import os

class LockScreenApp(App):
    def build(self):
        self.time_remaining = 0
        self.timer_running = False
        
        layout = BoxLayout(orientation='vertical', padding=20, spacing=20)
        
        # 标题
        title = Label(text='计时锁屏', font_size=32, bold=True)
        layout.add_widget(title)
        
        # 时间输入
        time_layout = BoxLayout(spacing=10)
        time_label = Label(text='设置时间（分钟）:', font_size=20)
        self.time_input = TextInput(text='10', font_size=20, multiline=False)
        time_layout.add_widget(time_label)
        time_layout.add_widget(self.time_input)
        layout.add_widget(time_layout)
        
        # 倒计时显示
        self.countdown_label = Label(text='倒计时: 00:00', font_size=40, bold=True)
        layout.add_widget(self.countdown_label)
        
        # 按钮
        button_layout = BoxLayout(spacing=20)
        self.start_button = Button(text='开始', font_size=24, on_press=self.start_timer)
        self.stop_button = Button(text='停止', font_size=24, on_press=self.stop_timer)
        button_layout.add_widget(self.start_button)
        button_layout.add_widget(self.stop_button)
        layout.add_widget(button_layout)
        
        # 状态信息
        self.status_label = Label(text='请设置时间并点击开始', font_size=16)
        layout.add_widget(self.status_label)
        
        return layout
    
    def start_timer(self, instance):
        try:
            minutes = int(self.time_input.text)
            if minutes <= 0:
                self.status_label.text = '请输入大于0的时间'
                return
            
            self.time_remaining = minutes * 60
            self.timer_running = True
            self.status_label.text = '倒计时已开始'
            self.start_button.disabled = True
            
            # 启动时钟
            Clock.schedule_interval(self.update_countdown, 1)
        except ValueError:
            self.status_label.text = '请输入有效的数字'
    
    def stop_timer(self, instance):
        self.timer_running = False
        self.start_button.disabled = False
        self.status_label.text = '倒计时已停止'
        Clock.unschedule(self.update_countdown)
        self.countdown_label.text = '倒计时: 00:00'
    
    def update_countdown(self, dt):
        if self.time_remaining > 0:
            self.time_remaining -= 1
            minutes = self.time_remaining // 60
            seconds = self.time_remaining % 60
            self.countdown_label.text = f'倒计时: {minutes:02d}:{seconds:02d}'
        else:
            self.timer_running = False
            self.start_button.disabled = False
            self.status_label.text = '时间到，正在锁屏...'
            Clock.unschedule(self.update_countdown)
            self.lock_screen()
    
    def lock_screen(self):
        # 不同Android设备的锁屏命令
        lock_commands = [
            'input keyevent 26',  # 电源键
            'am start -a android.intent.action.SCREEN_OFF',
            'svc power lock'
        ]
        
        # 尝试执行锁屏命令
        for cmd in lock_commands:
            try:
                if os.name == 'posix':
                    subprocess.run(['adb', 'shell', cmd], check=True, capture_output=True)
                elif os.name == 'nt':
                    # Windows环境下模拟锁屏
                    subprocess.run(['rundll32.exe', 'user32.dll,LockWorkStation'], check=True)
                break
            except:
                continue
        
        self.status_label.text = '已尝试锁屏'

if __name__ == '__main__':
    LockScreenApp().run()
