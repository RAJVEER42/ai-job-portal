import React from 'react';
import { X } from 'lucide-react';

const SkillBadge = ({ 
  skill, 
  variant = 'default', 
  size = 'medium', 
  removable = false, 
  onRemove,
  count,
  className = ''
}) => {
  const variants = {
    default: 'bg-gray-100 text-gray-800 border-gray-200',
    primary: 'bg-blue-100 text-blue-800 border-blue-200',
    secondary: 'bg-purple-100 text-purple-800 border-purple-200',
    success: 'bg-green-100 text-green-800 border-green-200',
    warning: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    danger: 'bg-red-100 text-red-800 border-red-200',
    outline: 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
  };

  const sizes = {
    small: 'px-2 py-1 text-xs',
    medium: 'px-3 py-1 text-sm',
    large: 'px-4 py-2 text-base'
  };

  const baseClasses = 'inline-flex items-center gap-1 rounded-full border transition-colors font-medium';
  const variantClasses = variants[variant] || variants.default;
  const sizeClasses = sizes[size] || sizes.medium;

  return (
    <span className={`${baseClasses} ${variantClasses} ${sizeClasses} ${className}`}>
      <span className="truncate">
        {skill}
        {count && (
          <span className="ml-1 opacity-75">
            ({count})
          </span>
        )}
      </span>
      
      {removable && onRemove && (
        <button
          type="button"
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onRemove(skill);
          }}
          className="ml-1 hover:bg-black/10 rounded-full p-0.5 transition-colors"
          aria-label={`Remove ${skill}`}
        >
          <X className="w-3 h-3" />
        </button>
      )}
    </span>
  );
};

// Skill Badge Group Component for displaying multiple skills
export const SkillBadgeGroup = ({ 
  skills, 
  maxVisible = 5, 
  variant = 'default', 
  size = 'medium',
  removable = false,
  onRemove,
  showCount = false,
  className = ''
}) => {
  if (!skills || skills.length === 0) {
    return null;
  }

  const skillArray = Array.isArray(skills) 
    ? skills 
    : skills.split(',').map(skill => skill.trim()).filter(Boolean);

  const visibleSkills = skillArray.slice(0, maxVisible);
  const remainingCount = skillArray.length - maxVisible;

  return (
    <div className={`flex flex-wrap gap-2 ${className}`}>
      {visibleSkills.map((skill, index) => (
        <SkillBadge
          key={`${skill}-${index}`}
          skill={skill}
          variant={variant}
          size={size}
          removable={removable}
          onRemove={onRemove}
          count={showCount ? 1 : undefined}
        />
      ))}
      
      {remainingCount > 0 && (
        <SkillBadge
          skill={`+${remainingCount} more`}
          variant="outline"
          size={size}
        />
      )}
    </div>
  );
};

// Interactive Skill Input Component
export const SkillInput = ({ 
  skills = [], 
  onSkillsChange, 
  placeholder = "Add skills...",
  suggestions = [],
  maxSkills = 20
}) => {
  const [inputValue, setInputValue] = React.useState('');
  const [showSuggestions, setShowSuggestions] = React.useState(false);
  const [filteredSuggestions, setFilteredSuggestions] = React.useState([]);

  React.useEffect(() => {
    if (inputValue && suggestions.length > 0) {
      const filtered = suggestions
        .filter(suggestion => 
          suggestion.toLowerCase().includes(inputValue.toLowerCase()) &&
          !skills.some(skill => skill.toLowerCase() === suggestion.toLowerCase())
        )
        .slice(0, 8);
      setFilteredSuggestions(filtered);
      setShowSuggestions(filtered.length > 0);
    } else {
      setShowSuggestions(false);
    }
  }, [inputValue, suggestions, skills]);

  const addSkill = (skill) => {
    const trimmedSkill = skill.trim();
    if (trimmedSkill && !skills.includes(trimmedSkill) && skills.length < maxSkills) {
      onSkillsChange([...skills, trimmedSkill]);
      setInputValue('');
      setShowSuggestions(false);
    }
  };

  const removeSkill = (skillToRemove) => {
    onSkillsChange(skills.filter(skill => skill !== skillToRemove));
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && inputValue.trim()) {
      e.preventDefault();
      addSkill(inputValue);
    } else if (e.key === 'Backspace' && !inputValue && skills.length > 0) {
      removeSkill(skills[skills.length - 1]);
    }
  };

  return (
    <div className="relative">
      <div className="border border-gray-300 rounded-lg p-3 min-h-[2.5rem] focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-transparent">
        <div className="flex flex-wrap gap-2 items-center">
          <SkillBadgeGroup
            skills={skills}
            variant="primary"
            size="small"
            removable
            onRemove={removeSkill}
          />
          
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={skills.length === 0 ? placeholder : ''}
            className="flex-1 min-w-[120px] outline-none text-sm"
            disabled={skills.length >= maxSkills}
          />
        </div>
      </div>

      {showSuggestions && (
        <div className="absolute top-full left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg z-10 mt-1 max-h-48 overflow-y-auto">
          {filteredSuggestions.map((suggestion, index) => (
            <button
              key={index}
              onClick={() => addSkill(suggestion)}
              className="w-full text-left px-3 py-2 hover:bg-gray-50 text-sm border-b border-gray-100 last:border-b-0"
            >
              {suggestion}
            </button>
          ))}
        </div>
      )}

      {skills.length >= maxSkills && (
        <p className="text-xs text-gray-500 mt-1">
          Maximum {maxSkills} skills allowed
        </p>
      )}
    </div>
  );
};

export default SkillBadge;