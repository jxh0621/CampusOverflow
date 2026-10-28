import { useState } from 'react';
import { useAuth } from '../auth';
import { api } from '../api';

export function ProfilePage() {
  const { me, refresh } = useAuth();
  const [college, setCollege] = useState(me?.college ?? '');
  const [className, setClassName] = useState(me?.className ?? '');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  if (!me) return <p className="muted">加载中…</p>;

  const save = async () => {
    setSaving(true); setMsg(''); setError('');
    try {
      await api.updateProfile({ college, className });
      await refresh();
      setMsg('资料已更新');
    } catch (e: any) {
      setError(e.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="form-page narrow">
      <h1>个人资料</h1>
      <p className="muted">{me.displayName} · {me.username} · {me.role}</p>

      <label>学院<input value={college} onChange={(e) => setCollege(e.target.value)} /></label>
      <label>班级<input value={className} onChange={(e) => setClassName(e.target.value)} /></label>

      <p className="muted">声誉积分：{me.id > 0 ? '见排行榜页' : ''}</p>

      {msg && <p className="ok">{msg}</p>}
      {error && <p className="err">{error}</p>}
      <button className="primary" onClick={save} disabled={saving}>
        {saving ? '保存中…' : '保存'}
      </button>
    </div>
  );
}
