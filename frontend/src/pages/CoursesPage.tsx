import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

interface Course {
  id: number;
  code: string;
  name: string;
  term: string;
}

export function CoursesPage() {
  const [mine, setMine] = useState<Course[]>([]);
  const [all, setAll] = useState<Course[]>([]);
  const [joinCode, setJoinCode] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.myCourses().then(setMine).catch(() => undefined);
    api.courses().then(setAll).catch(() => undefined);
  }, []);

  const join = async () => {
    setMsg(''); setError('');
    if (!joinCode.trim()) { setError('请输入课程码'); return; }
    try {
      await api.joinCourse(joinCode.trim());
      setMsg('加入成功！');
      setJoinCode('');
      const refreshed = await api.myCourses();
      setMine(refreshed);
    } catch (e: any) {
      setError(e.message || '加入失败');
    }
  };

  return (
    <div className="layout">
      <section>
        <h2>我的课程</h2>
        {mine.length === 0 ? <p className="muted">还没有加入任何课程，用下方课程码加入。</p> : (
          <ul className="card-list">
            {mine.map((c) => (
              <li key={c.id}>
                <Link to={`/?courseId=${c.id}`}>
                  <b>{c.name}</b> <span className="muted">{c.code} · {c.term}</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>加入课程</h2>
        <div className="join-bar">
          <input placeholder="输入课程码，如 CS2001" value={joinCode}
                 onChange={(e) => setJoinCode(e.target.value)} />
          <button onClick={join}>加入</button>
        </div>
        {msg && <p className="ok">{msg}</p>}
        {error && <p className="err">{error}</p>}
      </section>

      <section>
        <h2>全部课程</h2>
        <ul className="card-list">
          {all.map((c) => (
            <li key={c.id}>
              <b>{c.name}</b> <span className="muted">{c.code} · {c.term}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
