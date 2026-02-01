import { AlertCircle, CheckCircle, Info, XCircle } from 'lucide-react';

const Alert = ({ type = 'info', title, message, onClose }) => {
  const styles = {
    info: {
      bg: 'bg-blue-50',
      border: 'border-blue-200',
      text: 'text-blue-800',
      icon: <Info className="w-5 h-5 text-blue-500" />,
    },
    success: {
      bg: 'bg-green-50',
      border: 'border-green-200',
      text: 'text-green-800',
      icon: <CheckCircle className="w-5 h-5 text-green-500" />,
    },
    warning: {
      bg: 'bg-yellow-50',
      border: 'border-yellow-200',
      text: 'text-yellow-800',
      icon: <AlertCircle className="w-5 h-5 text-yellow-500" />,
    },
    error: {
      bg: 'bg-red-50',
      border: 'border-red-200',
      text: 'text-red-800',
      icon: <XCircle className="w-5 h-5 text-red-500" />,
    },
  };

  const style = styles[type];

  return (
    <div className={`${style.bg} ${style.border} border rounded-lg p-4 flex gap-3`}>
      <div className="flex-shrink-0">{style.icon}</div>
      <div className="flex-1">
        {title && <h4 className={`font-semibold ${style.text}`}>{title}</h4>}
        {message && <p className={`text-sm ${style.text} ${title ? 'mt-1' : ''}`}>{message}</p>}
      </div>
      {onClose && (
        <button onClick={onClose} className="flex-shrink-0 text-gray-400 hover:text-gray-600">
          <XCircle className="w-5 h-5" />
        </button>
      )}
    </div>
  );
};

export default Alert;
